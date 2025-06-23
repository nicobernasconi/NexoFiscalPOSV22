package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.GrisClaro
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun rememberFormattedDate(dateString: String?): String {
    return remember(dateString) {
        if (dateString.isNullOrBlank()) {
            "Sin fecha"
        } else {
            try {
                // Intenta parsear el formato completo primero
                val parserCompleto = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                formatter.format(parserCompleto.parse(dateString)!!)
            } catch (e: Exception) {
                try {
                    // Si falla, intenta con solo la fecha
                    val parserFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formatterFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatterFecha.format(parserFecha.parse(dateString)!!)
                } catch (e2: Exception) {
                    dateString // Si todo falla, devuelve el string original
                }
            }
        }
    }
}

@Composable
fun ComprobantesScreen(
    comprobanteViewModel: ComprobanteViewModel,
    renglonComprobanteViewModel: RenglonComprobanteViewModel,
    clienteViewModel: ClienteViewModel,
    productoViewModel: ProductoViewModel,
    onDismiss: () -> Unit
) {
    val pagedComprobantes = comprobanteViewModel.pagedComprobantes.collectAsLazyPagingItems()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedComprobanteConDetalle by remember { mutableStateOf<ComprobanteConDetalle?>(null) }
    var selectedRenglones by remember { mutableStateOf<List<RenglonComprobante>>(emptyList()) }

    CrudListScreen(
        title = "Comprobantes Guardados",
        items = pagedComprobantes,
        itemContent = { item ->
            val comprobante = item.comprobante
            val fechaFormateada = rememberFormattedDate(comprobante.fecha)

            Column {
                Text(text = item.tipoComprobante?.nombre?.uppercase() ?: "COMPROBANTE", style = MaterialTheme.typography.bodySmall)
                Text(text = "Nº: ${comprobante.numeroFactura ?: comprobante.numero ?: comprobante.id}", fontWeight = FontWeight.Bold)
                Text(text = "Fecha: $fechaFormateada", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Cliente: ${item.cliente?.nombre ?: comprobante.nombreCliente ?: "Consumidor Final"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Monto: $${comprobante.total ?: "0.00"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        },
        onSearchQueryChanged = { query ->
            comprobanteViewModel.search(query)
        },
        onSelect = { item ->
            scope.launch {
                // CAMBIO: Usamos item.comprobante.localId en lugar de item.comprobante.id
                selectedRenglones = renglonComprobanteViewModel.getRenglonesByComprobanteId(item.comprobante.localId)
                selectedComprobanteConDetalle = item
                showDetailDialog = true
            }
        },
        // --- CAMBIO PRINCIPAL: Lógica de reimpresión actualizada ---
        onAttemptEdit = { item ->
            scope.launch {
                // --- CORRECCIÓN ---
                // Cambia item.comprobante.id por item.comprobante.localId
                val renglonesCompletos = renglonComprobanteViewModel.getRenglonesByComprobanteId(item.comprobante.localId)

                PrintingManager.print(
                    context = context,
                    comprobante = item.comprobante,
                    renglones = renglonesCompletos
                )
            }
        },
        screenMode = CrudScreenMode.EDIT_DELETE_EDIT_PRINT,
        itemKey = { item ->
            // Usa el ID local del comprobante, que es la clave primaria en la base de datos
            // y se garantiza que sea única para cada fila.
            "comprobante_${item.comprobante.localId}"
        },
        onDelete = null,
        searchHint = stringResource(R.string.search_items),
        onDismiss = onDismiss
    )

    if (showDetailDialog && selectedComprobanteConDetalle != null) {
        ComprobanteDetailDialog(
            item = selectedComprobanteConDetalle!!,
            renglones = selectedRenglones,
            onDismiss = { showDetailDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComprobanteDetailDialog(
    item: ComprobanteConDetalle,
    renglones: List<RenglonComprobante>,
    onDismiss: () -> Unit
) {
    val comprobante = item.comprobante
    val fechaFormateada = rememberFormattedDate(comprobante.fecha)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f), shape = MaterialTheme.shapes.large) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Detalle del Comprobante") },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                    item {
                        Text(
                            text = "${item.tipoComprobante?.nombre?.uppercase() ?: "COMPROBANTE"} N° ${comprobante.numeroFactura ?: comprobante.numero}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Fecha: $fechaFormateada", style = MaterialTheme.typography.bodyLarge)
                        Text("Cliente: ${item.cliente?.nombre ?: comprobante.nombreCliente ?: "Consumidor Final"}", style = MaterialTheme.typography.bodyLarge)
                        item.cliente?.cuit?.let {
                            if(it.isNotBlank()) Text("CUIT/DNI: $it", style = MaterialTheme.typography.bodyLarge)
                        }
                        Text("Total: $${comprobante.total}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Productos", style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(bottom = 8.dp))
                    }

                    items(renglones) { renglon ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = GrisClaro.copy(alpha = 0.5f))) {
                            ListItem(
                                headlineContent = { Text(renglon.descripcion, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(String.format(Locale.getDefault(), "Cant: %.2f x $%.2f", renglon.cantidad.toDouble(), renglon.precioUnitario)) },
                                trailingContent = { Text(renglon.totalLinea, fontWeight = FontWeight.Bold) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}