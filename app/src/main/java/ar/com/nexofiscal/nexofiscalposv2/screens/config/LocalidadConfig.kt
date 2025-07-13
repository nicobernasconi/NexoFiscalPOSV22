package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProvinciaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal

fun getLocalidadFieldDescriptors(
    provinciaViewModel: ProvinciaViewModel
): List<FieldDescriptor<Localidad>> {
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
                    label = "Nombre de la Localidad",
                    isReadOnly = isReadOnly,
                    error = error
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
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.codigoPostal ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(codigoPostal = newValue) } },
                    label = "Código Postal",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "provincia",
            label = "Provincia",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }

                // CAMBIO: Se reemplaza DropdownSelectionField por el control personalizado.
                EntitySelectionButton(
                    label = "Provincia",
                    selectedValue = entity.provincia?.nombre,
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                if (showDialog) {
                    val pagedProvincias = provinciaViewModel.pagedProvincias.collectAsLazyPagingItems()
                    // CAMBIO: Se utiliza el SelectionModal genérico.
                    SelectionModal(
                        title = "Seleccionar Provincia",
                        pagedItems = pagedProvincias,
                        itemContent = { item -> Text(item.nombre ?: "") },
                        onSearch = { provinciaViewModel.search(it) },
                        onSelect = { seleccion ->
                            // CAMBIO: Se utiliza la lambda de actualización atómica.
                            onUpdate { it.copy(provincia = seleccion) }
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
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