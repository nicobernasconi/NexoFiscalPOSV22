package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PrintingStatusDialog() {
    val printingState by PrintingUiManager.state.collectAsState()

    if (printingState is PrintingState.Idle) {
        return // No mostrar nada si está inactivo.
    }

    Dialog(
        onDismissRequest = {
            // Solo se puede cerrar si hay un error.
            if (printingState is PrintingState.Error) {
                PrintingUiManager.finishPrinting()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = printingState is PrintingState.Error,
            dismissOnClickOutside = printingState is PrintingState.Error
        )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (val state = printingState) {
                    is PrintingState.InProgress -> {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Imprimiendo comprobante...", style = MaterialTheme.typography.titleMedium)
                    }
                    is PrintingState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error de Impresión", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { PrintingUiManager.finishPrinting() }, shape = RoundedCornerShape(5.dp)) {
                            Text("Aceptar")
                        }
                    }
                    is PrintingState.Idle -> {}
                }
            }
        }
    }
}