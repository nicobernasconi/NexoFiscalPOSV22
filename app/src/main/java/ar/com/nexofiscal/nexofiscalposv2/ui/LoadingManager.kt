// LoadingManager.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager de estado de carga. Emití true al iniciar
 * cualquier petición, y false al terminar.
 */
object LoadingManager {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Llamalo antes de empezar una petición */
    fun show() {
        _isLoading.value = true
    }

    /** Llamalo al terminar (éxito o error) */
    fun hide() {
        _isLoading.value = false
    }
}
