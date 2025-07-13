package ar.com.nexofiscal.nexofiscalposv2

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.managers.SyncManager
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class SyncService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var prefs: SharedPreferences
    private lateinit var broadcaster: LocalBroadcastManager

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "SyncServiceChannel"
        const val SYNC_STATE_ACTION = "ar.com.nexofiscal.SYNC_STATE_UPDATE"
        const val EXTRA_SYNC_STATE = "EXTRA_SYNC_STATE"
        const val ACTION_START_PERIODIC_SYNC = "ACTION_START_PERIODIC_SYNC"
        const val ACTION_TRIGGER_UPLOAD_ONCE = "ACTION_TRIGGER_UPLOAD_ONCE"
    }

    enum class SyncState {
        IDLE, DOWNLOADING, UPLOADING
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("nexofiscal_config", Context.MODE_PRIVATE)
        broadcaster = LocalBroadcastManager.getInstance(this)
        Log.i("SyncService", "Servicio creado.")
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("NexoFiscal POS")
            .setContentText("Servicio de sincronización activo.")
            .setSmallIcon(R.drawable.ic_inventory) // Reemplazar con un ícono adecuado
            .build()

        startForeground(1, notification)
        when (intent?.action) {
            ACTION_TRIGGER_UPLOAD_ONCE -> {
                Log.i("SyncService", "Recibida solicitud para subida única.")
                scope.launch {
                    val token = SessionManager.token
                    if (!token.isNullOrBlank()) {
                        broadcastSyncState(SyncState.UPLOADING)
                        UploadManager.uploadLocalChanges(applicationContext, token)
                        broadcastSyncState(SyncState.IDLE)
                    } else {
                        Log.e("SyncService", "No se puede ejecutar la subida única: token nulo.")
                    }
                    // Si no hay más tareas, el servicio puede detenerse.
                    // Opcional: podrías comprobar si las tareas periódicas están activas.
                }
            }
            else -> { // Por defecto, inicia las tareas periódicas
                Log.i("SyncService", "Iniciando tareas de sincronización periódicas.")
                startSyncJobs()
            }
        }

        Log.i("SyncService", "Servicio iniciado en primer plano.")
        startSyncJobs()

        return START_STICKY
    }

    private fun startSyncJobs() {
        val token = SessionManager.token

        if (token.isNullOrBlank()) {
            Log.e("SyncService", "No se pudo iniciar la sincronización, token nulo.")
            stopSelf()
            return
        }

        // Tarea de descarga periódica
        scope.launch {
            while (isActive) {
                val downloadInterval = prefs.getInt("tiempo_descarga", 15).toLong()
                Log.d("SyncService", "Próxima descarga en $downloadInterval minutos.")
                delay(TimeUnit.MINUTES.toMillis(downloadInterval))
                try {
                    broadcastSyncState(SyncState.DOWNLOADING)
                    Log.i("SyncService", "Iniciando descarga periódica...")
                    SyncManager.startFullSync(applicationContext, token)
                    Log.i("SyncService", "Descarga periódica finalizada.")
                } catch (e: Exception) {
                    Log.e("SyncService", "Error en descarga periódica", e)
                } finally {
                    broadcastSyncState(SyncState.IDLE)
                }
            }
        }

        // Tarea de subida periódica
        scope.launch {
            while (isActive) {
                val uploadInterval = prefs.getInt("tiempo_subida", 5).toLong()
                Log.d("SyncService", "Próxima subida en $uploadInterval minutos.")
                delay(TimeUnit.MINUTES.toMillis(uploadInterval))
                try {
                    broadcastSyncState(SyncState.UPLOADING)
                    Log.i("SyncService", "Iniciando subida periódica de cambios...")
                    UploadManager.uploadLocalChanges(applicationContext, token)
                    Log.i("SyncService", "Subida periódica finalizada.")
                } catch (e: Exception) {
                    Log.e("SyncService", "Error en subida periódica", e)
                } finally {
                    broadcastSyncState(SyncState.IDLE)
                }
            }
        }
    }

    private fun broadcastSyncState(state: SyncState) {
        val intent = Intent(SYNC_STATE_ACTION)
        intent.putExtra(EXTRA_SYNC_STATE, state.name)
        broadcaster.sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.i("SyncService", "Servicio destruido.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Servicio de Sincronización",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}