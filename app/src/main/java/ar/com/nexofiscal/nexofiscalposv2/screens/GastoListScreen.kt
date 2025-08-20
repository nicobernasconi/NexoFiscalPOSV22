package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.nexofiscal.nexofiscalposv2.db.entity.GastoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoGastoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.GastoViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import android.app.DatePickerDialog
import android.widget.DatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoListScreen(
    onDismiss: () -> Unit,
    vm: GastoViewModel = viewModel()
) {
    // Filtros
    val (defaultDesde, defaultHasta) = vm.rangoHoy()
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val ymdFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var fechaDesdeDate by remember { mutableStateOf(ymdFormat.parse(defaultDesde.substring(0, 10)) ?: Date()) }
    var fechaHastaDate by remember { mutableStateOf(ymdFormat.parse(defaultHasta.substring(0, 10)) ?: Date()) }

    // Selector de fecha reutilizable (similar a InformeDeVentas)
    val datePickerDialog = remember(context) {
        { onDateSelected: (Date) -> Unit ->
            DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    calendar.set(year, month, dayOfMonth)
                    onDateSelected(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    var tipos by remember { mutableStateOf<List<TipoGastoEntity>>(emptyList()) }
    var tipoSelectedIndex by remember { mutableStateOf(-1) }
    var tipoDropdownExpanded by remember { mutableStateOf(false) }

    var resultados by remember { mutableStateOf<List<GastoEntity>>(emptyList()) }
    var total by remember { mutableStateOf(0.0) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { tipos = vm.listarTiposGasto() } catch (_: Exception) {}
        val items = vm.buscarGastos(
            desde = "${ymdFormat.format(fechaDesdeDate)} 00:00:00",
            hasta = "${ymdFormat.format(fechaHastaDate)} 23:59:59",
            tipoId = currentTipoId(tipos, tipoSelectedIndex)
        )
        resultados = items
        total = items.sumOf { it.monto ?: 0.0 }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Gastos", style = MaterialTheme.typography.headlineSmall)

            // Filtros: Fechas y Tipo (al estilo InformeDeVentas)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    EntitySelectionButton(
                        label = "Fecha Desde",
                        selectedValue = dateFormat.format(fechaDesdeDate),
                        isReadOnly = false,
                        error = null
                    ) {
                        datePickerDialog { fecha -> fechaDesdeDate = fecha }.show()
                    }
                }
                Box(Modifier.weight(1f)) {
                    EntitySelectionButton(
                        label = "Fecha Hasta",
                        selectedValue = dateFormat.format(fechaHastaDate),
                        isReadOnly = false,
                        error = null
                    ) {
                        datePickerDialog { fecha -> fechaHastaDate = fecha }.show()
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = tipoDropdownExpanded,
                    onExpandedChange = { tipoDropdownExpanded = it }
                ) {
                    val tipoLabel = when {
                        tipoSelectedIndex < 0 -> "Todos los tipos"
                        tipoSelectedIndex in tipos.indices -> "${tipos[tipoSelectedIndex].id} - ${tipos[tipoSelectedIndex].nombre}"
                        else -> "Todos los tipos"
                    }
                    OutlinedTextField(
                        readOnly = true,
                        value = tipoLabel,
                        onValueChange = {},
                        label = { Text("Tipo de gasto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tipoDropdownExpanded,
                        onDismissRequest = { tipoDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los tipos") },
                            onClick = { tipoSelectedIndex = -1; tipoDropdownExpanded = false }
                        )
                        tipos.forEachIndexed { index, t ->
                            val itemLabel = "${t.id} - ${t.nombre}"
                            DropdownMenuItem(
                                text = { Text(itemLabel) },
                                onClick = { tipoSelectedIndex = index; tipoDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val items = vm.buscarGastos(
                                desde = "${ymdFormat.format(fechaDesdeDate)} 00:00:00",
                                hasta = "${ymdFormat.format(fechaHastaDate)} 23:59:59",
                                tipoId = currentTipoId(tipos, tipoSelectedIndex)
                            )
                            resultados = items
                            total = items.sumOf { it.monto ?: 0.0 }
                        }
                    },
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Buscar") }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Cerrar") }
                Spacer(Modifier.weight(1f))
                Text("Total: $${String.format(Locale.getDefault(), "%.2f", total)}", style = MaterialTheme.typography.titleMedium)
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(resultados) { g ->
                    GastoRow(g)
                }
            }
        }
    }
}

@Composable
private fun GastoRow(g: GastoEntity) {
    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(g.descripcion ?: "Sin descripción", style = MaterialTheme.typography.bodyLarge)
                val subtitulo = buildString {
                    append(g.fecha ?: "")
                    g.tipoGasto?.let { if (it.isNotBlank()) append("  ·  ").append(it) }
                }
                if (subtitulo.isNotBlank()) Text(subtitulo, style = MaterialTheme.typography.bodySmall)
            }
            Text("$${String.format(Locale.getDefault(), "%.2f", g.monto ?: 0.0)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun currentTipoId(tipos: List<TipoGastoEntity>, index: Int): Int? =
    if (index in tipos.indices) tipos[index].id else null
