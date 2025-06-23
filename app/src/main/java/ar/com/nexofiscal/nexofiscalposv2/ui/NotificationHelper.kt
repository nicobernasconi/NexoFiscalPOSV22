package ar.com.nexofiscal.nexofiscalposv2.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.managers.SyncProgress

object NotificationHelper {

    private const val CHANNEL_ID = "sync_channel"
    private const val SYNC_NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        val name = "Canal de Sincronización"
        val descriptionText = "Notificaciones para el estado de la sincronización de datos"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildProgressNotification(context: Context, progress: SyncProgress): Notification {
        val progressPercentage = (progress.overallTaskIndex * 100) / progress.totalTasks
        val title = "Sincronizando Datos (${progressPercentage}%)"
        val content = "Tarea: ${progress.currentTaskName} (${progress.currentTaskItemCount} registros)"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_inventory) // Asegúrate de tener este ícono
            .setOngoing(true) // Hace que la notificación no se pueda descartar
            .setOnlyAlertOnce(true)
            .setProgress(progress.totalTasks, progress.overallTaskIndex, false)
            .build()
    }

    fun updateSyncNotification(context: Context, progress: SyncProgress) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!progress.isFinished) {
            notificationManager.notify(SYNC_NOTIFICATION_ID, buildProgressNotification(context, progress))
        } else {
            // Cuando termina, cancelamos la de progreso y mostramos una final.
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
            showCompletionNotification(context, progress)
        }
    }

    private fun showCompletionNotification(context: Context, progress: SyncProgress) {
        val title = "Sincronización Finalizada"
        val content = if (progress.errors.isEmpty()) {
            "Todos los datos se han actualizado correctamente."
        } else {
            "Proceso finalizado con ${progress.errors.size} errores."
        }

        val finalNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(if (progress.errors.isEmpty()) R.drawable.ic_check else R.drawable.ic_warning)
            .setAutoCancel(true) // La notificación se cierra al tocarla
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SYNC_NOTIFICATION_ID + 1, finalNotification)
    }
}