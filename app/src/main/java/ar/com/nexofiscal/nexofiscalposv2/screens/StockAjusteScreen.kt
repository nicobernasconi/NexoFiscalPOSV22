package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.StockAjusteViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.utils.MoneyUtils
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

// Helper de cálculo de precio 1 a partir de costo y margen (%).
private fun computePrecio1ByMargen(costo: Double, margen: Double): String {
    // margen esperado como porcentaje (por ejemplo, 30.0 => +30%)
    val precio = costo * (1.0 + (margen / 100.0))
    return String.format(Locale.US, "%.2f", precio)
}

// Definición de estado de fila a nivel de archivo para usar en la lista y filas
class AjusteRowState(val producto: ProductoEntity, stock: Double) {
    var stockActual by mutableStateOf(stock)
    var cantidadText by mutableStateOf("")
    var costoText by mutableStateOf(String.format(Locale.US, "%.2f", producto.precioCosto))
    var actualizarPrecio1 by mutableStateOf(false)
    var precio1Text by mutableStateOf(String.format(Locale.US, "%.2f", producto.precio1))
    var isApplying by mutableStateOf(false)
}

@Composable
fun StockAjusteScreen(
    onDismiss: () -> Unit,
    vm: StockAjusteViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var codigoOBarras by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var applyingAll by remember { mutableStateOf(false) }

    val items = remember { mutableStateListOf<AjusteRowState>() }

    fun validarCantidad(c: String): Boolean {
        val norm = c.replace(',', '.')
        return norm.matches(Regex("^-?\\d*\\.?\\d{0,3}$"))
    }

    fun validarPrecio(p: String): Boolean {
        val norm = p.replace(',', '.')
        return norm.matches(Regex("^\\d*\\.?\\d{0,2}$"))
    }

    fun agregarOAumentarFila(producto: ProductoEntity, stock: Double) {
        val existente = items.firstOrNull { it.producto.id == producto.id }
        if (existente != null) {
            // Si ya existe, incrementa +1 de conveniencia
            val actual = existente.cantidadText.toDoubleOrNull() ?: 0.0
            existente.cantidadText = String.format(Locale.US, "%.3f", actual + 1.0)
            NotificationManager.show("Cantidad incrementada para ${producto.descripcion}", NotificationType.INFO)
        } else {
            items.add(AjusteRowState(producto, stock))
            NotificationManager.show("Agregado: ${producto.descripcion}", NotificationType.SUCCESS)
        }
    }

    suspend fun agregarPorCodigoOBarras() {
        if (codigoOBarras.isBlank()) {
            NotificationManager.show("Ingrese un código o código de barras.", NotificationType.WARNING)
            return
        }
        isAdding = true
        try {
            val p = vm.buscarProductoPorCodigoOBarra(codigoOBarras)
            if (p == null) {
                NotificationManager.show("Producto no encontrado.", NotificationType.WARNING)
            } else {
                val stock = vm.cargarStockActual(p)
                agregarOAumentarFila(p, stock)
            }
        } catch (e: Exception) {
            NotificationManager.show(e.message ?: "Error al buscar producto.", NotificationType.ERROR)
        } finally {
            isAdding = false
            codigoOBarras = ""
        }
    }

    suspend fun aplicarFila(row: AjusteRowState) {
        val cant = row.cantidadText.replace(',', '.').toDoubleOrNull()
        if (cant == null || cant == 0.0) {
            NotificationManager.show("Cantidad inválida o 0 para ${row.producto.descripcion}", NotificationType.WARNING)
            return
        }
        val nuevoCosto = row.costoText.replace(',', '.').toDoubleOrNull()
        val nuevoPrecio1 = if (row.actualizarPrecio1) row.precio1Text.replace(',', '.').toDoubleOrNull() else null
        row.isApplying = true
        try {
            val nuevoStock = vm.aplicarAjuste(
                context = context,
                producto = row.producto,
                cantidadAjuste = cant,
                nuevoCosto = nuevoCosto,
                actualizarPrecio1 = row.actualizarPrecio1,
                nuevoPrecio1 = nuevoPrecio1
            )
            row.stockActual = nuevoStock
            // Reset cantidad para evitar re-aplicar por error
            row.cantidadText = ""
            NotificationManager.show("Ajuste aplicado a ${row.producto.descripcion}. Nuevo stock: ${MoneyUtils.format(nuevoStock)}", NotificationType.SUCCESS)
        } catch (e: Exception) {
            NotificationManager.show(e.message ?: "Error al aplicar el ajuste.", NotificationType.ERROR)
        } finally {
            row.isApplying = false
        }
    }

    suspend fun aplicarTodos() {
        if (items.isEmpty()) {
            NotificationManager.show("No hay productos en la lista.", NotificationType.INFO)
            return
        }
        applyingAll = true
        try {
            var aplicados = 0
            for (row in items) {
                val cant = row.cantidadText.replace(',', '.').toDoubleOrNull()
                if (cant == null || cant == 0.0) continue
                try {
                    val nuevoCosto = row.costoText.replace(',', '.').toDoubleOrNull()
                    val nuevoPrecio1 = if (row.actualizarPrecio1) row.precio1Text.replace(',', '.').toDoubleOrNull() else null
                    val nuevoStock = vm.aplicarAjuste(
                        context = context,
                        producto = row.producto,
                        cantidadAjuste = cant,
                        nuevoCosto = nuevoCosto,
                        actualizarPrecio1 = row.actualizarPrecio1,
                        nuevoPrecio1 = nuevoPrecio1
                    )
                    row.stockActual = nuevoStock
                    row.cantidadText = ""
                    aplicados++
                } catch (e: Exception) {
                    NotificationManager.show("Error en ${row.producto.descripcion}: ${e.message}", NotificationType.ERROR)
                }
            }
            NotificationManager.show("Ajustes aplicados: $aplicados", NotificationType.SUCCESS)
        } finally {
            applyingAll = false
        }
    }

    val headerTextStyle = MaterialTheme.typography.labelSmall
    val cellPadding = 6.dp
    val rowHeight = 44.dp

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título compacto
            Text("Ajuste de stock", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            // Agregar por código/código de barras (compacto)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = codigoOBarras,
                    onValueChange = { codigoOBarras = it },
                    label = { Text("Código o barras", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { scope.launch { agregarPorCodigoOBarras() } }),
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                )
                Spacer(Modifier.width(6.dp))
                Button(
                    onClick = { scope.launch { agregarPorCodigoOBarras() } },
                    enabled = !isAdding,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                    Spacer(Modifier.width(6.dp))
                    Text(if (isAdding) "Buscando..." else "Agregar", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Encabezado de tabla
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("Producto", style = headerTextStyle, modifier = Modifier.weight(1.6f))
                Text("Stock", style = headerTextStyle, modifier = Modifier.weight(0.8f))
                Text("Cant.", style = headerTextStyle, modifier = Modifier.weight(0.9f))
                Text("Costo", style = headerTextStyle, modifier = Modifier.weight(0.9f))
                Text("P1?", style = headerTextStyle, modifier = Modifier.weight(0.5f))
                Text("Nuevo P1", style = headerTextStyle, modifier = Modifier.weight(0.9f))
                Text("", style = headerTextStyle, modifier = Modifier.width(64.dp))
            }

            // Lista compacta
            if (items.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Agregue productos para ajustar", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items, key = { it.producto.id }) { row ->
                        TableRowCompact(
                            row = row,
                            onCantidadChange = { if (validarCantidad(it)) row.cantidadText = it.replace(',', '.') },
                            onCostoChange = { input ->
                                if (validarPrecio(input)) {
                                    row.costoText = input.replace(',', '.')
                                    val costoVal = row.costoText.toDoubleOrNull()
                                    if (row.actualizarPrecio1 && costoVal != null) {
                                        row.precio1Text = computePrecio1ByMargen(costoVal, row.producto.margenGanancia)
                                    }
                                }
                            },
                            onTogglePrecio1 = { checked ->
                                row.actualizarPrecio1 = checked
                                val costoVal = row.costoText.toDoubleOrNull()
                                if (checked && costoVal != null) {
                                    row.precio1Text = computePrecio1ByMargen(costoVal, row.producto.margenGanancia)
                                }
                            },
                            onPrecio1Change = { if (validarPrecio(it)) row.precio1Text = it.replace(',', '.') },
                            onAplicar = { scope.launch { aplicarFila(row) } },
                            onEliminar = { items.remove(row) },
                            cellPadding = cellPadding,
                            rowHeight = rowHeight
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                // Acciones globales compactas
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                        Spacer(Modifier.width(6.dp))
                        Text("Cerrar", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { items.clear(); NotificationManager.show("Lista limpiada", NotificationType.INFO) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Limpiar")
                        Spacer(Modifier.width(6.dp))
                        Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { scope.launch { aplicarTodos() } },
                        enabled = !applyingAll,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Aplicar todos")
                        Spacer(Modifier.width(6.dp))
                        Text(if (applyingAll) "Aplicando..." else "Aplicar todos", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRowCompact(
    row: AjusteRowState,
    onCantidadChange: (String) -> Unit,
    onCostoChange: (String) -> Unit,
    onTogglePrecio1: (Boolean) -> Unit,
    onPrecio1Change: (String) -> Unit,
    onAplicar: () -> Unit,
    onEliminar: () -> Unit,
    cellPadding: Dp,
    rowHeight: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = rowHeight).padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Producto
        Column(Modifier.weight(1.6f).padding(end = cellPadding)) {
            Text(
                text = "${row.producto.codigo ?: ""} - ${row.producto.descripcion ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Stock: ${MoneyUtils.format(row.stockActual)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stock (solo lectura)
        Text(
            text = MoneyUtils.format(row.stockActual),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.8f).padding(horizontal = cellPadding)
        )

        // Cantidad (sin +/-)
        OutlinedTextField(
            value = row.cantidadText,
            onValueChange = onCantidadChange,
            singleLine = true,
            placeholder = { Text("0", style = MaterialTheme.typography.labelSmall) },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.9f).heightIn(min = 40.dp)
        )

        // Costo (sin +/-)
        OutlinedTextField(
            value = row.costoText,
            onValueChange = onCostoChange,
            singleLine = true,
            placeholder = { Text("0.00", style = MaterialTheme.typography.labelSmall) },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.9f).heightIn(min = 40.dp)
        )

        // Check Precio1
        Row(Modifier.weight(0.5f), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = row.actualizarPrecio1, onCheckedChange = onTogglePrecio1)
        }

        // Nuevo Precio1 (sin +/-)
        OutlinedTextField(
            value = row.precio1Text,
            onValueChange = onPrecio1Change,
            enabled = row.actualizarPrecio1,
            singleLine = true,
            placeholder = { Text("0.00", style = MaterialTheme.typography.labelSmall) },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.9f).heightIn(min = 40.dp)
        )

        // Acciones
        Row(Modifier.width(64.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onAplicar, modifier = Modifier.clip(RoundedCornerShape(5.dp))) { Icon(imageVector = Icons.Filled.Check, contentDescription = "Aplicar") }
            IconButton(onClick = onEliminar, modifier = Modifier.clip(RoundedCornerShape(5.dp))) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar") }
        }
    }
}
