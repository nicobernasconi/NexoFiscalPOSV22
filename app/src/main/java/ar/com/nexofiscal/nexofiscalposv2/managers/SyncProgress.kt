package ar.com.nexofiscal.nexofiscalposv2.managers

/**
 * Representa el estado actual de un proceso de sincronización de varias tareas.
 *
 * @param currentTaskName Nombre de la tarea actual (ej: "Productos").
 * @param currentTaskItemCount Cantidad de ítems descargados para la tarea actual.
 * @param overallTaskIndex El índice de la tarea actual (ej: 3 de 10).
 * @param totalTasks El número total de tareas en la cola de sincronización.
 * @param errors Lista de mensajes de error que han ocurrido.
 * @param isFinished Verdadero si todo el proceso de sincronización ha finalizado.
 */
data class SyncProgress(
    val currentTaskName: String = "Iniciando...",
    val currentTaskItemCount: Int = 0,
    val overallTaskIndex: Int = 0,
    val totalTasks: Int = 1,
    val errors: List<String> = emptyList(),
    val isFinished: Boolean = false
)