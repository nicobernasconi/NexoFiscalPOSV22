package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoFormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.PagedSelectionDialog
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

// Componente genérico para un campo de texto que abre un diálogo de selección
@Composable
fun DropdownSelectionField(
    label: String,
    currentValue: String,
    isReadOnly: Boolean,
    error: String?,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = currentValue,
        onValueChange = {},
        modifier = Modifier.clickable(enabled = !isReadOnly, onClick = onClick),
        label = { Text(label) },
        readOnly = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) }
    )
}

// Se elimina la función privada y obsoleta SelectionDialog

fun getFormaPagoFieldDescriptors(
    tipoFormaPagoViewModel: TipoFormaPagoViewModel
): List<FieldDescriptor<FormaPago>> {
    return listOf(
        FieldDescriptor(
            id = "id",
            label = "ID",
            editorContent = { entity, _, _, _ ->
                OutlinedTextField(
                    value = entity.id.toString(),
                    onValueChange = {},
                    label = { Text("ID") },
                    readOnly = true
                )
            },
            isReadOnly = { true }
        ),
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Forma de Pago") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly
                )
            },
            validator = { if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "porcentaje",
            label = "Porcentaje",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.porcentaje.toString(),
                    onValueChange = { onUpdate(entity.copy(porcentaje = it.toIntOrNull() ?: 0)) },
                    label = { Text("Porcentaje Recargo/Descuento") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "tipoFormaPago",
            label = "Tipo de Forma de Pago",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                // CORRECCIÓN: Usar el flujo paginado
                val pagedItems = tipoFormaPagoViewModel.pagedTiposFormaPago.collectAsLazyPagingItems()

                DropdownSelectionField(
                    label = "Tipo de Forma de Pago",
                    currentValue = entity.tipoFormaPago?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                // CORRECCIÓN: Usar el nuevo diálogo paginado
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Tipo",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { query -> tipoFormaPagoViewModel.search(query) },
                    onSelect = {
                        onUpdate(entity.copy(tipoFormaPago = it))
                        showDialog = false
                    }
                )
            },
            validator = { if (it.tipoFormaPago == null) ValidationResult.Invalid("Debe seleccionar un tipo.") else ValidationResult.Valid }
        )
    )
}