package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Bloquea la activación por teclado (Enter / NumpadEnter) en componentes interactivos.
 */
fun Modifier.blockEnterToClick(): Modifier = this.onPreviewKeyEvent { event ->
    val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
    if (isEnter && (event.type == KeyEventType.KeyDown || event.type == KeyEventType.KeyUp)) {
        true // Consumir para que no dispare click
    } else {
        false
    }
}

