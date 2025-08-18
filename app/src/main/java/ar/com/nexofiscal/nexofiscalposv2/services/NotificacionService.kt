package ar.com.nexofiscal.nexofiscalposv2.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.NotificacionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificacionService : Service() {

    private val job: Job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val prefsName = "nexofiscal_notif"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("NotificacionService", "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            while (isActive) {
                try {
                    processActivasOnce()
                } catch (e: Exception) {
                    Log.e("NotificacionService", "Error procesando notificaciones", e)
                }
                // Revisa cada 60s por nuevas notificaciones activas
                delay(60_000)
            }
        }
        return START_STICKY
    }

    private suspend fun processActivasOnce() {
        val dao = AppDatabase.getInstance(applicationContext).notificacionDao()
        val activas = dao.getActivas()
        if (activas.isEmpty()) return

        val pendingToShow = activas.filter { shouldShow(it) }
        if (pendingToShow.isEmpty()) return

        Log.i("NotificacionService", "Mostrando ${pendingToShow.size} notificaciones activas")
        pendingToShow.forEachIndexed { idx, n ->
            showDialog(n)
            markShown(n)
            // Evita lanzar múltiples actividades exactamente a la vez
            delay(500L * (idx + 1))
        }
    }

    private fun showDialog(n: NotificacionEntity) {
        val durationMs = when (n.tipoNotificacionId) {
            3 -> 60_000L // Crítica: más tiempo
            2 -> 8_000L  // Aviso
            else -> 4_000L // Información
        }
        val intent = Intent(applicationContext, NotificacionDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(NotificacionDialogActivity.EXTRA_TITLE, n.nombre ?: "")
            putExtra(NotificacionDialogActivity.EXTRA_MESSAGE, n.mensaje ?: "")
            putExtra(NotificacionDialogActivity.EXTRA_TIPO, n.tipoNotificacionId ?: 1)
            putExtra(NotificacionDialogActivity.EXTRA_DURATION, durationMs)
        }
        startActivity(intent)
    }

    private fun notifId(n: NotificacionEntity): String = n.serverId?.toString() ?: "local_${n.id}"

    private fun getLastShown(id: String): Long {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        return prefs.getLong("last_shown_$id", 0L)
    }

    private fun setLastShown(id: String, whenMs: Long) {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().putLong("last_shown_$id", whenMs).apply()
    }

    private fun shouldShow(n: NotificacionEntity): Boolean {
        val id = notifId(n)
        val last = getLastShown(id)
        val now = System.currentTimeMillis()
        return when (n.tipoNotificacionId ?: 1) {
            1 -> last == 0L // Tipo 1: solo una vez
            2 -> last == 0L || now - last >= 5 * 60 * 60 * 1000L // Tipo 2: cada 5 horas
            3 -> last == 0L || now - last >= 10 * 60 * 1000L // Tipo 3: cada 10 minutos
            else -> last == 0L
        }
    }

    private fun markShown(n: NotificacionEntity) {
        val id = notifId(n)
        setLastShown(id, System.currentTimeMillis())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.i("NotificacionService", "Servicio destruido")
    }
}
