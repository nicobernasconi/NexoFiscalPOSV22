package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaResumenView
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CierreCajaViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.GrisClaro
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo
import ar.com.nexofiscal.nexofiscalposv2.utils.MoneyUtils
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreCajaListScreen(
    viewModel: CierreCajaViewModel,
    onDismiss: () -> Unit
) {
    val cierres = viewModel.cierresResumenPaginated().collectAsLazyPagingItems()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listado de Cierres de Caja", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulNexo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
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
    cierre: CierreCajaResumenView,
    onPrint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GrisClaro)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Cierre #${cierre.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            cierre.fecha?.let { Text("Fecha: $it", style = MaterialTheme.typography.bodyMedium) }
            cierre.usuarioNombreCompleto?.let { Text("Usuario: $it", style = MaterialTheme.typography.bodyMedium) }
            cierre.comentarios?.takeIf { it.isNotBlank() }?.let { Text("Comentario: $it", style = MaterialTheme.typography.bodyMedium) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Efectivo inicial: $${MoneyUtils.format(cierre.efectivoInicial ?: 0.0)}")
                Text("Efectivo final: $${MoneyUtils.format(cierre.efectivoFinal ?: 0.0)}")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onPrint, shape = RoundedCornerShape(5.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_print),
                        contentDescription = "Imprimir",
                        tint = Blanco
                    )

                }
            }
        }
    }
}
