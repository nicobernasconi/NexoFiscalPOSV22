package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoGastoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.GastoViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoAddScreen(
    onDismiss: () -> Unit,
    vm: GastoViewModel = viewModel()
) {
    var descripcion by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }

    var tipos by remember { mutableStateOf<List<TipoGastoEntity>>(emptyList()) }
    var tipoSelectedIndex by remember { mutableStateOf(-1) }
    var tipoDropdownExpanded by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            tipos = vm.listarTiposGasto()
        } catch (_: Exception) { /* ignore */ }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Agregar Gasto", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = montoText,
                onValueChange = { input ->
                    val normalized = input.replace(',', '.')
                    if (normalized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        montoText = normalized
                    }
                },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Selector de Tipo de Gasto (opcional)
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = tipoDropdownExpanded,
                    onExpandedChange = { tipoDropdownExpanded = it }
                ) {
                    val tipoLabel = when {
                        tipoSelectedIndex < 0 -> "Sin tipo"
                        tipoSelectedIndex in tipos.indices -> "${tipos[tipoSelectedIndex].id} - ${tipos[tipoSelectedIndex].nombre}"
                        else -> "Sin tipo"
                    }
                    OutlinedTextField(
                        readOnly = true,
                        value = tipoLabel,
                        onValueChange = {},
                        label = { Text("Tipo de gasto (opcional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tipoDropdownExpanded,
                        onDismissRequest = { tipoDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin tipo") },
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

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val monto = montoText.toDoubleOrNull() ?: 0.0
                        if (descripcion.isBlank() || monto <= 0.0) {
                            NotificationManager.show("Complete descripción y monto válido.", NotificationType.WARNING)
                            return@Button
                        }
                        isSaving = true
                        val tipoId = if (tipoSelectedIndex in tipos.indices) tipos[tipoSelectedIndex].id else null
                        vm.guardarGasto(
                            descripcion = descripcion,
                            monto = monto,
                            tipoGastoId = tipoId
                        )
                        NotificationManager.show("Gasto agregado.", NotificationType.SUCCESS)
                        onDismiss()
                        isSaving = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(5.dp)
                ) { Text(if (isSaving) "Guardando..." else "Guardar") }
            }
        }
    }
}
