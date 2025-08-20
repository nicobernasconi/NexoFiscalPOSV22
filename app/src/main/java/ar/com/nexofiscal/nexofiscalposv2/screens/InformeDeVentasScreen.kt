package ar.com.nexofiscal.nexofiscalposv2.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeDeVentasScreen(
    onDismiss: () -> Unit,
    clienteViewModel: ClienteViewModel,
    tipoComprobanteViewModel: TipoComprobanteViewModel,
    usuarioViewModel: UsuarioViewModel,
    vendedorViewModel: VendedorViewModel
) {
    val informeViewModel: InformeDeVentasViewModel = viewModel()
    val filtros by informeViewModel.filtros.collectAsState()
    val resultados by informeViewModel.resultados.collectAsState()
    val context = LocalContext.current

    // --- Estados para controlar la visibilidad de los diálogos de selección ---
    var showClienteDialog by remember { mutableStateOf(false) }
    var showTipoComprobanteDialog by remember { mutableStateOf(false) }
    var showUsuarioDialog by remember { mutableStateOf(false) }
    var showVendedorDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val datePickerDialog = { onDateSelected: (Date) -> Unit ->
        DatePickerDialog(context, { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.time)
        },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informe de Ventas") },
                navigationIcon = { IconButton(onClick = onDismiss, modifier = Modifier.clip(RoundedCornerShape(5.dp))) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = { IconButton(onClick = { /* Lógica de impresión */ }, modifier = Modifier.clip(RoundedCornerShape(5.dp))) { Icon(Icons.Default.Print, "Imprimir") } }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { EntitySelectionButton("Fecha Desde", filtros.fechaDesde?.let { dateFormat.format(it) }, false, null) { datePickerDialog { fecha -> informeViewModel.actualizarFiltro(filtros.copy(fechaDesde = fecha)) }.show() } }
                    Box(Modifier.weight(1f)) { EntitySelectionButton("Fecha Hasta", filtros.fechaHasta?.let { dateFormat.format(it) }, false, null) { datePickerDialog { fecha -> informeViewModel.actualizarFiltro(filtros.copy(fechaHasta = fecha)) }.show() } }
                }
                EntitySelectionButton("Tipo de Comprobante", filtros.tipoComprobante?.nombre, false, null) { showTipoComprobanteDialog = true }
                EntitySelectionButton("Cliente", filtros.cliente?.nombre, false, null) { showClienteDialog = true }
                EntitySelectionButton("Vendedor", filtros.vendedor?.nombre, false, null) { showVendedorDialog = true }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { informeViewModel.ejecutarInforme() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(5.dp)) { Text("Generar Informe") }
                OutlinedButton(onClick = { informeViewModel.limpiarFiltros() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(5.dp)) { Text("Limpiar Filtros") }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            if (resultados.comprobantes.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resultados", style = MaterialTheme.typography.titleLarge)
                    Text("Total: $${String.format(Locale.getDefault(), "%.2f", resultados.totalVentas)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                            Text("Tipo/Número", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Fecha", Modifier.weight(0.7f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Monto", Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                        Divider()
                    }
                    items(resultados.comprobantes, key = { it.comprobante.localId }) { detalle ->
                        val comprobante = detalle.comprobante
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("${detalle.tipoComprobante?.nombre ?: "CPN"} Nº ${comprobante.numeroFactura ?: ""}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(rememberFormattedDate(comprobante.fecha), Modifier.weight(0.7f), style = MaterialTheme.typography.bodyMedium)
                            Text("$${comprobante.total}", Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ajuste los filtros y genere un informe para ver los resultados.", textAlign = TextAlign.Center)
                }
            }
        }
    }

    // --- Diálogos Modales para Selección ---
    if (showClienteDialog) {
        val pagedItems = clienteViewModel.pagedClientes.collectAsLazyPagingItems()
        SelectionModal("Seleccionar Cliente", pagedItems, { Text(it.nombre ?: "") }, { clienteViewModel.search(it) }, { informeViewModel.actualizarFiltro(filtros.copy(cliente = it)); showClienteDialog = false }, { showClienteDialog = false }, {it.id})
    }
    if (showTipoComprobanteDialog) {
        val pagedItems = tipoComprobanteViewModel.pagedTiposComprobante.collectAsLazyPagingItems()
        SelectionModal("Seleccionar Tipo", pagedItems, { Text(it.nombre ?: "") }, { tipoComprobanteViewModel.search(it) }, { informeViewModel.actualizarFiltro(filtros.copy(tipoComprobante = it)); showTipoComprobanteDialog = false }, { showTipoComprobanteDialog = false }, {it.id})
    }
    if (showVendedorDialog) {
        val pagedItems = vendedorViewModel.pagedVendedores.collectAsLazyPagingItems()
        SelectionModal("Seleccionar Vendedor", pagedItems, { Text(it.nombre ?: "") }, { vendedorViewModel.search(it) }, { informeViewModel.actualizarFiltro(filtros.copy(vendedor = it)); showVendedorDialog = false }, { showVendedorDialog = false }, {it.id})
    }
}

@Composable
private fun rememberFormattedDate(dateString: String?): String {
    return remember(dateString) {
        if (dateString.isNullOrBlank()) "Sin fecha"
        else {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                parser.parse(dateString)?.let { formatter.format(it) } ?: dateString
            } catch (e: Exception) { dateString }
        }
    }
}