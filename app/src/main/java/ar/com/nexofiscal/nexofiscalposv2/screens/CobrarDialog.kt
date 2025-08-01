package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.FormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PromocionViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo
import java.util.*
import kotlin.math.absoluteValue

data class Pago(
    val formaPago: FormaPago,
    var monto: Double = 0.0,
    val id: Int = UUID.randomUUID().hashCode()
)

// --- NUEVA CLASE DE DATOS ---
data class ResultadoCobro(
    val pagos: List<Pago>,
    val promociones: List<Promocion>
)
// --------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CobrarScreen(
    totalAPagar: Double,
    formaPagoViewModel: FormaPagoViewModel,
    promocionViewModel: PromocionViewModel,
    onDismiss: () -> Unit,
    // --- PARÁMETROS DE LAMBDA ACTUALIZADOS ---
    onGuardar: (ResultadoCobro) -> Unit,
    onImprimir: (ResultadoCobro) -> Unit
) {
    val pagosSeleccionados = remember { mutableStateListOf<Pago>() }
    val promocionesSeleccionadas = remember { mutableStateListOf<Promocion>() }
    val recargosPorPago = remember { mutableStateMapOf<Int, Double>() }

    val todasLasFormasDePago by formaPagoViewModel.allFormasPago.collectAsState()

    LaunchedEffect(todasLasFormasDePago) {
        if (pagosSeleccionados.isEmpty() && todasLasFormasDePago.isNotEmpty()) {
            val todasLasFormasDePago = formaPagoViewModel.getAllFormasDePagoCompletas()

            // 2. Ahora, esta línea funcionará perfectamente.
            val formaDePagoPorDefecto = todasLasFormasDePago.find { it.tipoFormaPago?.id == 1 }

            if (formaDePagoPorDefecto != null) {
                // Haz lo que necesites con la forma de pago encontrada.
            }
            formaDePagoPorDefecto?.let {
                pagosSeleccionados.add(
                    Pago(
                        formaPago = it,
                        monto = 0.0
                    )
                )
            }
        }
    }

    val recargosAcumulados by remember { derivedStateOf { recargosPorPago.values.sum() } }
    val descuentosAcumulados by remember {
        derivedStateOf {
            if (promocionesSeleccionadas.isEmpty()) 0.0
            else totalAPagar * (promocionesSeleccionadas.sumOf { it.porcentaje } / 100.0)
        }
    }
    val totalAjustes by remember { derivedStateOf { recargosAcumulados - descuentosAcumulados } }
    val totalIngresado by remember { derivedStateOf { pagosSeleccionados.sumOf { it.monto } } }
    val totalFinalACobrar by remember { derivedStateOf { totalAPagar + totalAjustes } }
    val balance by remember { derivedStateOf { totalIngresado - totalFinalACobrar } }

    var showFormaPagoSelector by remember { mutableStateOf(false) }
    var showPromocionSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cobrar Venta") }, navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") } }) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cobroCompleto = balance.absoluteValue < 0.01 && pagosSeleccionados.isNotEmpty()

                Button(
                    onClick = {
                        val resultado = ResultadoCobro(pagosSeleccionados.toList(), promocionesSeleccionadas.toList())
                        onGuardar(resultado)
                    },
                    enabled = cobroCompleto,
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NegroNexo,
                        contentColor = Blanco
                    ),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("Guardar Venta")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        val resultado = ResultadoCobro(pagosSeleccionados.toList(), promocionesSeleccionadas.toList())
                        onImprimir(resultado)
                    },
                    enabled = cobroCompleto,
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NegroNexo,
                        contentColor = Blanco
                    ),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_print), null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar e Imprimir")
                }
            }
        }
    ) { paddingValues ->
        Row(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            // --- COLUMNA IZQUIERDA (LISTAS) ---
            LazyColumn(modifier = Modifier.weight(2f).padding(end = 16.dp)) {
                // Sección de Promociones
                item {
                    Text("Promociones Aplicadas", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (promocionesSeleccionadas.isEmpty()) {
                        Text("Ninguna", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            promocionesSeleccionadas.forEach { promo ->
                                InputChip(
                                    selected = true,
                                    onClick = { /* No action on click */ },
                                    label = { Text("${promo.nombre} (-${promo.porcentaje}%)") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Quitar promoción",
                                            modifier = Modifier.size(18.dp).clickable { promocionesSeleccionadas.remove(promo) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Text("Formas de Pago", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Lista de Formas de Pago
                itemsIndexed(pagosSeleccionados, key = { _, item -> item.id }) { index, pago ->
                    val isLocked = index < pagosSeleccionados.size - 1
                    PagoItemRow(
                        pago = pago,
                        isLocked = isLocked,
                        onMontoChanged = { newMonto -> pagosSeleccionados[index] = pago.copy(monto = newMonto) },
                        onRemove = {
                            recargosPorPago.remove(pago.id)
                            pagosSeleccionados.remove(pago)
                        },
                        totalFinalACobrar = totalFinalACobrar,
                        totalIngresado = totalIngresado
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // --- COLUMNA DERECHA (RESUMEN Y BOTONES) ---
            Column(modifier = Modifier.weight(1f)) {
                // --- Resumen de Totales ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total Venta:", style = MaterialTheme.typography.titleSmall)
                    Text(String.format(Locale.getDefault(), "$%.2f", totalAPagar), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Text("Ajustes (Rec/Dto):", style = MaterialTheme.typography.titleSmall)
                    Text(String.format(Locale.getDefault(), "$%.2f", totalAjustes), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Divider()

                    Text("Total a Cobrar:", style = MaterialTheme.typography.titleLarge)
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalFinalACobrar)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider()

                    Text("Total Ingresado:", style = MaterialTheme.typography.titleSmall)
                    Text(String.format(Locale.getDefault(), "$%.2f", totalIngresado), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    if (balance < -0.001) {
                        Text("FALTAN:", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                        Text(String.format(Locale.getDefault(), "$%.2f", -balance), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    } else if (balance > 0.001) {
                        Text("VUELTO:", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                        Text(String.format(Locale.getDefault(), "$%.2f", balance), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    }
                }

                // --- Botones de Acción (Anclados abajo) ---
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val ultimoPago = pagosSeleccionados.lastOrNull()
                        if (ultimoPago != null) {
                            val faltante = totalFinalACobrar - totalIngresado + ultimoPago.monto
                            pagosSeleccionados[pagosSeleccionados.lastIndex] = ultimoPago.copy(monto = faltante)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulNexo),
                    shape = BordeSuave,
                    enabled = pagosSeleccionados.isNotEmpty()
                ) { Text("Completar importe") }

                Button(
                    onClick = { showPromocionSelector = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                    shape = BordeSuave
                ) { Text("Agregar Promoción") }

                Button(
                    onClick = { showFormaPagoSelector = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = BordeSuave
                ) { Text("Agregar Pago") }
            }
        }
    }

    if (showFormaPagoSelector) {
        val pagedFormasPago = formaPagoViewModel.pagedFormasPago.collectAsLazyPagingItems()
        SelectionModal(
            title = "Seleccionar Forma de Pago",
            pagedItems = pagedFormasPago,
            itemContent = { item -> Text("${item.nombre} (${item.porcentaje}%)") },
            onSearch = { query -> formaPagoViewModel.search(query) },
            onSelect = { formaPago ->
                // La lógica de selección se mantiene igual
                val faltanteActual = totalFinalACobrar - totalIngresado
                val recargoNuevo = faltanteActual * (formaPago.porcentaje / 100.0)
                val nuevoPago = Pago(formaPago = formaPago)
                recargosPorPago[nuevoPago.id] = recargoNuevo
                pagosSeleccionados.add(nuevoPago)
                showFormaPagoSelector = false
            },
            onDismiss = { showFormaPagoSelector = false },
            itemKey = { it.localId } // Usamos localId para consistencia
        )
    }

    // Diálogo para seleccionar la PROMOCIÓN
    if (showPromocionSelector) {
        val pagedPromociones = promocionViewModel.pagedPromociones.collectAsLazyPagingItems()
        SelectionModal(
            title = "Seleccionar Promoción",
            pagedItems = pagedPromociones,
            itemContent = { item -> Text("${item.nombre} (-${item.porcentaje}%)") },
            onSearch = { query -> promocionViewModel.search(query) },
            onSelect = { promo ->
                // La lógica de selección se mantiene igual
                if (!promocionesSeleccionadas.any { it.id == promo.id }) {
                    promocionesSeleccionadas.add(promo)
                }
                showPromocionSelector = false
            },
            onDismiss = { showPromocionSelector = false },
            itemKey = { it.localId } // Usamos localId para consistencia
        )
    }
}


@Composable
fun PagoItemRow(
    pago: Pago, isLocked: Boolean, onMontoChanged: (Double) -> Unit, onRemove: () -> Unit,
    totalFinalACobrar: Double, totalIngresado: Double
) {
    // Estado para el contenido del campo de texto
    var montoText by remember { mutableStateOf(if (pago.monto == 0.0) "" else String.format(Locale.US, "%.2f", pago.monto)) }
    // Estado para saber si el campo está en foco
    var isFocused by remember { mutableStateOf(false) }

    // Este efecto sincroniza el campo con el modelo, PERO SOLO SI NO ESTÁ EN FOCO.
    // Esto evita que se sobreescriba la entrada del usuario.
    LaunchedEffect(pago.monto) {
        if (!isFocused) {
            montoText = if (pago.monto == 0.0) "" else String.format(Locale.US, "%.2f", pago.monto)
        }
    }

    OutlinedTextField(
        value = montoText,
        onValueChange = { newText ->
            // Valida que el texto sea un número válido con hasta 2 decimales
            if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                // Actualiza el texto local para que el usuario vea lo que escribe
                montoText = newText
                // Notifica el cambio hacia arriba para que los totales se actualicen en tiempo real
                onMontoChanged(newText.toDoubleOrNull() ?: 0.0)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            // Detecta cuándo el campo gana o pierde el foco
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                // Al perder el foco, formatea el texto a 2 decimales para consistencia visual
                if (!isFocused) {
                    montoText = String.format(Locale.US, "%.2f", pago.monto)
                }
            },
        label = { Text("${pago.formaPago.nombre?.uppercase(Locale.getDefault())} (${pago.formaPago.porcentaje}%)") },
        readOnly = isLocked,
        trailingIcon = {
            IconButton(onClick = onRemove, enabled = !isLocked) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}