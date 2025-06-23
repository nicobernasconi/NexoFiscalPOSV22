package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProvinciaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.screens.PagedSelectionDialog
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getLocalidadFieldDescriptors(
    provinciaViewModel: ProvinciaViewModel
): List<FieldDescriptor<Localidad>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Localidad") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                )
            },
            validator = {
                if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.")
                else ValidationResult.Valid
            }
        ),
        FieldDescriptor(
            id = "codigoPostal",
            label = "Código Postal",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.codigoPostal ?: "",
                    onValueChange = { onUpdate(entity.copy(codigoPostal = it)) },
                    label = { Text("Código Postal") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "provincia",
            label = "Provincia",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedProvincias = provinciaViewModel.pagedProvincias.collectAsLazyPagingItems()

                DropdownSelectionField(
                    label = "Provincia",
                    currentValue = entity.provincia?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                if (showDialog) {
                    PagedSelectionDialog<Provincia>(
                        showDialog = showDialog,
                        onDismiss = { showDialog = false },
                        title = "Seleccionar Provincia",
                        items = pagedProvincias,
                        itemContent = { item -> Text(item.nombre ?: "") },
                        onSearch = { provinciaViewModel.search(it) },
                        onSelect = {
                            onUpdate(entity.copy(provincia = it))
                            showDialog = false
                        }
                    )
                }
            },
            validator = {
                if (it.provincia == null) ValidationResult.Invalid("Debe seleccionar una provincia.")
                else ValidationResult.Valid
            }
        )
    )
}