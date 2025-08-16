package ar.com.nexofiscal.nexofiscalposv2.services

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Worker que realiza un backup comprimido (ZIP) de la base de datos Room (nexofiscal.db).
 * Genera un archivo por día con el nombre de la empresa y timestamp. Si ya existe uno
 * para el día en curso, no crea uno nuevo.
 */
class BackupDatabaseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Asegurar que SessionManager esté inicializado en este proceso
            SessionManager.init(applicationContext)

            val companyRaw = SessionManager.empresaNombre?.takeIf { it.isNotBlank() } ?: "Empresa"
            val company = companyRaw.replace("[^A-Za-z0-9_-]".toRegex(), "_")

            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val time = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())

            val backupsDir = getBackupsDir(applicationContext)
            if (!backupsDir.exists()) backupsDir.mkdirs()

            // Si ya existe un backup para hoy, no hacemos nada
            val alreadyHasToday = backupsDir.listFiles()?.any { it.name.contains("_${today}_") } == true
            if (alreadyHasToday) return@withContext Result.success()

            val dbName = "nexofiscal.db"
            val dbFile = applicationContext.getDatabasePath(dbName)
            val dbShm = File(dbFile.absolutePath + "-shm")
            val dbWal = File(dbFile.absolutePath + "-wal")

            if (!dbFile.exists()) {
                // No hay base creada aún, salir sin error para no reintentar indefinidamente
                return@withContext Result.success()
            }

            val filesToZip = listOf(dbFile, dbShm, dbWal).filter { it.exists() }
            if (filesToZip.isEmpty()) return@withContext Result.success()

            val outName = "${company}_${today}_${time}.db.zip"
            val outFile = File(backupsDir, outName)

            zipFiles(filesToZip, outFile)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // En caso de error de E/S transitorio, permitir reintento
            Result.retry()
        }
    }

    private fun getBackupsDir(context: Context): File {
        val external = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return if (external != null) File(external, "backups") else File(context.filesDir, "backups")
    }

    private fun zipFiles(files: List<File>, outFile: File) {
        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
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

