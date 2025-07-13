package ar.com.nexofiscal.nexofiscalposv2.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Define los posibles estados de la interfaz de usuario durante la impresión.
 */
sealed class PrintingState {
    object Idle : PrintingState() // Inactivo, el diálogo está oculto.
    object InProgress : PrintingState() // Imprimiendo, muestra un spinner.
    data class Error(val message: String) : PrintingState() // Ocurrió un error.
}

/**
 * Gestor de estado para controlar el diálogo de impresión desde cualquier
 * parte de la aplicación.
 */
object PrintingUiManager {
    private val _state = MutableStateFlow<PrintingState>(PrintingState.Idle)
    val state = _state.asStateFlow()

    fun startPrinting() {
        _state.value = PrintingState.InProgress
    }

    fun showError(message: String) {
        _state.value = PrintingState.Error(message)
    }

    fun finishPrinting() {
        _state.value = PrintingState.Idle
    }
}