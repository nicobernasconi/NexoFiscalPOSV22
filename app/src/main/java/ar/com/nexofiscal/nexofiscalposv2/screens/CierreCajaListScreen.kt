package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CierreCajaViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CierreCajaListScreen(
    viewModel: CierreCajaViewModel,
    onDismiss: () -> Unit
) {
    val cierres = viewModel.cierresPaginated("").collectAsLazyPagingItems()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Listado de Cierres de Caja", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
            Spacer(Modifier.height(8.dp))

            when {
                cierres.itemCount == 0 && cierres.loadState.refresh is androidx.paging.LoadState.NotLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay cierres realizados.")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Iteración por índice para evitar extensiones adicionales
                        items(cierres.itemCount) { index ->
                            val cierre = cierres[index]
                            if (cierre != null) {
                                CierreItem(
                                    cierre = cierre,
                                    onPrint = {
                                        scope.launch {
                                            try {
                                                val (filtros, resumen) = viewModel.generarResumenCierre(cierre.id)
                                                PrintingManager.printCierreCaja(context, filtros, resumen)
                                                NotificationManager.show("Cierre #${cierre.id} enviado a impresión.", NotificationType.SUCCESS)
                                            } catch (e: Exception) {
                                                NotificationManager.show(e.message ?: "Error al imprimir el cierre.", NotificationType.ERROR)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            if (cierres.loadState.append is androidx.paging.LoadState.Loading || cierres.loadState.refresh is androidx.paging.LoadState.Loading) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CierreItem(
    cierre: CierreCajaEntity,
    onPrint: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Cierre #${cierre.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            cierre.fecha?.let { Text("Fecha: $it", style = MaterialTheme.typography.bodyMedium) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Efectivo inicial: ${String.format(Locale.getDefault(), "%.2f", cierre.efectivoInicial ?: 0.0)}")
                Text("Efectivo final: ${String.format(Locale.getDefault(), "%.2f", cierre.efectivoFinal ?: 0.0)}")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onPrint) { Text("Imprimir") }
            }
        }
    }
}
