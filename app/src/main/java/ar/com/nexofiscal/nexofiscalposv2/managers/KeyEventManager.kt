package ar.com.nexofiscal.nexofiscalposv2.managers

import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object KeyEventManager {

    private val _scannedCodeFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val scannedCodeFlow = _scannedCodeFlow.asSharedFlow()

    private val buffer = StringBuilder()
    private var lastEventTime = 0L

    fun processKeyEvent(event: KeyEvent): Boolean {
        // Solo nos interesan los eventos de "tecla presionada"
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val currentTime = System.currentTimeMillis()

        // Si ha pasado mucho tiempo desde el último evento, limpiamos el buffer.
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
                return true // ¡IMPORTANTE! Consumimos el evento "Enter" para que no active otras acciones.
            }
        } else {
            // Si es un carácter válido, lo añadimos al buffer.
            // No usamos event.unicodeChar porque podría no funcionar para todos los dispositivos.
            // En su lugar, usamos el keycode.
            val char = event.displayLabel
            if (char != null) {
                buffer.append(char)
            }
        }

        // Devolvemos 'false' para que el evento continúe su propagación normal
        // y llegue al campo de texto que tiene el foco.
        return false
    }
}