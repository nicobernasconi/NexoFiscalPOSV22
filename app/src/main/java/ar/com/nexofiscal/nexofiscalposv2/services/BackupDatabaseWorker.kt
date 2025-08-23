package ar.com.nexofiscal.nexofiscalposv2.services

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Worker que realiza un backup comprimido (ZIP) de la base de datos Room (nexofiscal.db).
 * Genera un archivo por día con el nombre de la empresa y timestamp. Si ya existe uno
 * para el día en curso, no crea uno nuevo.
 *
 * A partir de API 29 guarda en almacenamiento público (Downloads/NexoFiscal/backups) usando MediaStore,
 * lo cual persiste tras desinstalar. En API 28 usa la carpeta pública de Downloads.
 */
class BackupDatabaseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            SessionManager.init(applicationContext)
            val maxBackups = SessionManager.getMaxBackups().coerceAtLeast(1)
            val companyRaw = SessionManager.empresaNombre?.takeIf { it.isNotBlank() } ?: "Empresa"
            val company = companyRaw.replace("[^A-Za-z0-9_-]".toRegex(), "_")
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val time = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val dbName = "nexofiscal.db"
            val dbFile = applicationContext.getDatabasePath(dbName)
            val dbShm = File(dbFile.absolutePath + "-shm")
            val dbWal = File(dbFile.absolutePath + "-wal")
            if (!dbFile.exists()) return@withContext Result.success()
            val filesToZip = listOf(dbFile, dbShm, dbWal).filter { it.exists() }
            if (filesToZip.isEmpty()) return@withContext Result.success()
            val outName = "${company}_${today}_${time}.db.zip"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hadToday = alreadyHasTodayInDownloads(today)
                if (!hadToday) {
                    val uri = createDownloadsItemUri(outName)
                    if (uri != null) {
                        applicationContext.contentResolver.openOutputStream(uri, "w")?.use { os ->
                            zipFilesToStream(filesToZip, os)
                        } ?: return@withContext Result.retry()
                    }
                }
                // Poda siempre (haya o no nuevo backup)
                pruneMediaStoreBackups(maxBackups)
                return@withContext Result.success()
            }

            // API 28 y fallback
            val wroteToPublic = tryWriteToPublicDownloadsP(filesToZip, outName, today)
            if (!wroteToPublic) {
                val backupsDir = getAppBackupsDir(applicationContext)
                if (!backupsDir.exists()) backupsDir.mkdirs()
                val alreadyHasToday = backupsDir.listFiles()?.any { it.name.contains("_${today}_") } == true
                if (!alreadyHasToday) {
                    val outFile = File(backupsDir, outName)
                    FileOutputStream(outFile).use { fos -> zipFilesToStream(filesToZip, fos) }
                }
                pruneFileBackups(backupsDir, maxBackups)
            } else {
                // Poda en carpeta pública
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val backupsDir = File(downloads, "NexoFiscal/backups")
                pruneFileBackups(backupsDir, maxBackups)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    // ===== API 29+ (Q y superior) =====
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun alreadyHasTodayInDownloads(today: String): Boolean {
        val cr = applicationContext.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH
        )
        val relativePath = Environment.DIRECTORY_DOWNLOADS + "/NexoFiscal/backups/"
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(relativePath, "%_${today}_%")
        cr.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            return cursor.moveToFirst()
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createDownloadsItemUri(fileName: String): Uri? {
        val cr = applicationContext.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            // RELATIVE_PATH debe terminar con '/'
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NexoFiscal/backups/")
            // Visible inmediatamente en Q+
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        return cr.insert(collection, values)
    }

    // ===== API 28 (P) =====
    private fun tryWriteToPublicDownloadsP(filesToZip: List<File>, outName: String, today: String): Boolean {
        return try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupsDir = File(downloads, "NexoFiscal/backups")
            if (!backupsDir.exists()) backupsDir.mkdirs()

            val alreadyHasToday = backupsDir.listFiles()?.any { it.name.contains("_${today}_") } == true
            if (alreadyHasToday) return true

            val outFile = File(backupsDir, outName)
            FileOutputStream(outFile).use { fos ->
                zipFilesToStream(filesToZip, fos)
            }
            true
        } catch (_: SecurityException) {
            // Es probable que no se haya concedido WRITE_EXTERNAL_STORAGE en API 28
            false
        } catch (_: Exception) {
            false
        }
    }

    // ===== Utilidades comunes =====
    private fun getAppBackupsDir(context: Context): File {
        val external = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return if (external != null) File(external, "backups") else File(context.filesDir, "backups")
    }

    // Poda de backups (filesystem)
    private fun pruneFileBackups(dir: File, maxBackups: Int) {
        if (!dir.exists()) return
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".db.zip") }?.toList() ?: return
        if (files.size <= maxBackups) return
        // Ordenar por nombre descendente (fecha en nombre) y eliminar los más antiguos (al final)
        val sorted = files.sortedByDescending { it.name }
        sorted.drop(maxBackups).forEach { runCatching { it.delete() } }
    }

    // Poda de backups en MediaStore (API 29+)
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun pruneMediaStoreBackups(maxBackups: Int) {
        val cr = applicationContext.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val relativePath = Environment.DIRECTORY_DOWNLOADS + "/NexoFiscal/backups/"
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(relativePath, "%.db.zip")
        val backups = mutableListOf<Pair<String, Long>>() // name to id
        cr.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val name = cursor.getString(nameIdx)
                backups.add(name to id)
            }
        }
        if (backups.size <= maxBackups) return
        val sorted = backups.sortedByDescending { it.first }
        sorted.drop(maxBackups).forEach { (_, id) ->
            val uri = Uri.withAppendedPath(collection, id.toString())
            runCatching { cr.delete(uri, null, null) }
        }
    }

    private fun zipFilesToStream(files: List<File>, outStream: OutputStream) {
        ZipOutputStream(outStream).use { zos ->
            files.forEach { file ->
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }
}
