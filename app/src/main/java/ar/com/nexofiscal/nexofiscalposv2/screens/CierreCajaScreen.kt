package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CierreCajaViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import kotlinx.coroutines.launch

@Composable
fun CierreCajaScreen(
    viewModel: CierreCajaViewModel,
    onDismiss: () -> Unit
) {
    var efectivoInicial by remember { mutableStateOf("") }
    var efectivoFinal by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cierre de caja", style = MaterialTheme.typography.headlineSmall)
            Text("Ingrese los valores y confirme el cierre.", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = efectivoInicial,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")) ) efectivoInicial = it },
                label = { Text("Efectivo inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = efectivoFinal,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$")) ) efectivoFinal = it },
                label = { Text("Efectivo final") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        if (isLoading) return@Button
                        val ini = efectivoInicial.toDoubleOrNull() ?: 0.0
                        val fin = efectivoFinal.toDoubleOrNull() ?: 0.0
                        isLoading = true
                        scope.launch {
                            try {
                                val result = viewModel.cerrarCaja(ini, fin)

                                // Generar resumen y enviar a imprimir
                                val (filtros, resumen) = viewModel.generarResumenCierre(result.cierreId)
                                PrintingManager.printCierreCaja(context, filtros, resumen)

                                NotificationManager.show(
                                    "Cierre #${result.cierreId} realizado e impreso. Asignados: ${result.comprobantesAsignados}.",
                                    NotificationType.SUCCESS
                                )
                                onDismiss()
                            } catch (e: Exception) {
                                NotificationManager.show(
                                    e.message ?: "Error al realizar el cierre.",
                                    NotificationType.ERROR
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Cerrando..." else "Cerrar caja")
                }
            }
        }
    }
}
