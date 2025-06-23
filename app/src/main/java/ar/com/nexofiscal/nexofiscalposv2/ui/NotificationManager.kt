package ar.com.nexofiscal.nexofiscalposv2.ui

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** Tipos de notificación */
enum class NotificationType {
    SUCCESS, ERROR, INFO, WARNING
}

/** Datos de una notificación */
data class NotificationData(
    val message: String,
    val type: NotificationType
)

/**
 * Manager para emitir notificaciones.
 * Use `NotificationManager.show()` desde ViewModel, Activity o donde necesites.
 */
object NotificationManager {
    private val _events = MutableSharedFlow<NotificationData>(extraBufferCapacity = 1)
    val events: SharedFlow<NotificationData> = _events

    /** Emití una notificación (no bloqueante) */
    fun show(message: String, type: NotificationType) {
        GlobalScope.launch {
            _events.emit(NotificationData(message, type))
        }
    }
}
