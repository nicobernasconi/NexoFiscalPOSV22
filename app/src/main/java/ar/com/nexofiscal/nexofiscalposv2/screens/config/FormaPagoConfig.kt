package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoFormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal

fun getFormaPagoFieldDescriptors(
    tipoFormaPagoViewModel: TipoFormaPagoViewModel
): List<FieldDescriptor<FormaPago>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.nombre ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(nombre = newValue) } },
                    label = "Nombre de la Forma de Pago",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "porcentaje",
            label = "Porcentaje",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.porcentaje.toString(),
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(porcentaje = newValue.toIntOrNull() ?: 0) } },
                    label = "Porcentaje Recargo/Descuento",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "tipoFormaPago",
            label = "Tipo de Forma de Pago",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }

                // CAMBIO: Se reemplaza DropdownSelectionField por el control personalizado.
                EntitySelectionButton(
                    label = "Tipo de Forma de Pago",
                    selectedValue = entity.tipoFormaPago?.nombre,
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                if (showDialog) {
                    val pagedItems = tipoFormaPagoViewModel.pagedTiposFormaPago.collectAsLazyPagingItems()
                    // CAMBIO: Se utiliza el SelectionModal genérico.
                    SelectionModal(
                        title = "Seleccionar Tipo",
                        pagedItems = pagedItems,
                        itemContent = { item -> Text(item.nombre ?: "") },
                        onSearch = { query -> tipoFormaPagoViewModel.search(query) },
                        onSelect = { seleccion ->
                            // CAMBIO: Se utiliza la lambda de actualización atómica.
                            onUpdate { it.copy(tipoFormaPago = seleccion) }
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            },
            validator = { if (it.tipoFormaPago == null) ValidationResult.Invalid("Debe seleccionar un tipo.") else ValidationResult.Valid }
        )
    )
}