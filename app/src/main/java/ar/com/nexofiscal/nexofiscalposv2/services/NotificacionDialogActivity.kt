package ar.com.nexofiscal.nexofiscalposv2.services

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme

class NotificacionDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        val tipo = intent.getIntExtra(EXTRA_TIPO, 1)
        val durationMs = intent.getLongExtra(EXTRA_DURATION, 4000L)

        setContent {
            NexoFiscalPOSV2Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DialogContent(
                        title = title,
                        message = message,
                        tipo = tipo,
                        durationMs = durationMs,
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TIPO = "extra_tipo"
        const val EXTRA_DURATION = "extra_duration"
    }
}

@Composable
private fun DialogContent(
    title: String,
    message: String,
    tipo: Int,
    durationMs: Long,
    onClose: () -> Unit
) {
    val totalSeconds = (durationMs / 1000L).toInt().coerceAtLeast(1)
    var secondsLeft by remember { mutableStateOf(totalSeconds) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft--
        }
    }

    val (iconRes, tint, header) = when (tipo) {
        3 -> Triple(R.drawable.ic_warning, Color(0xFFB00020), "Crítica")
        2 -> Triple(R.drawable.ic_warning, Color(0xFFFFA000), "Aviso")
        else -> Triple(R.drawable.ic_info, Color(0xFF1976D2), "Información")
    }

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = header,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = title.ifBlank { header },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Se puede cerrar en ${secondsLeft}s",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onClose, enabled = secondsLeft <= 0) {
                Text(if (secondsLeft > 0) "Cerrar (${secondsLeft})" else "Cerrar")
            }
        }
    }
}

