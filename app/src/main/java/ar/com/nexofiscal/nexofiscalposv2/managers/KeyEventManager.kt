package ar.com.nexofiscal.nexofiscalposv2.managers

import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Un gestor centralizado para procesar los eventos de un escáner de código
 * de barras que emula un teclado.
 *
 * Captura los dígitos y los acumula en un buffer. Cuando detecta la tecla "Enter",
 * emite el código completo a través de un SharedFlow para que cualquier parte de la
 * app pueda suscribirse y recibir los códigos escaneados.
 */
object KeyEventManager {

    private val _scannedCodeFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val scannedCodeFlow = _scannedCodeFlow.asSharedFlow()

    private val buffer = StringBuilder()
    private var lastEventTime = 0L

    /**
     * Procesa un evento de teclado.
     *
     * @param event El KeyEvent que se ha producido.
     * @return `true` si el evento fue consumido, `false` en caso contrario.
     */
    fun processKeyEvent(event: KeyEvent): Boolean {
        // Solo nos interesan los eventos de "tecla presionada"
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val currentTime = System.currentTimeMillis()

        // Si ha pasado mucho tiempo desde el último evento, limpiamos el buffer.
        // Esto previene que se mezclen partes de distintos escaneos.
        if (currentTime - lastEventTime > 500) {
            buffer.clear()
        }
        lastEventTime = currentTime

        // Si se presiona "Enter", consideramos que el escaneo ha terminado.
        if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
            if (buffer.isNotEmpty()) {
                val scannedCode = buffer.toString()
                _scannedCodeFlow.tryEmit(scannedCode) // Emitimos el código
                buffer.clear() // Limpiamos el buffer para el siguiente escaneo
                return true // Indicamos que hemos manejado el evento
            }
        } else {
            // Si es un dígito, lo añadimos al buffer.
            val char = event.unicodeChar.toChar()
            if (char.isLetterOrDigit() || char in "-*./") {
                buffer.append(char)
                return true
            }
        }

        return false
    }
}