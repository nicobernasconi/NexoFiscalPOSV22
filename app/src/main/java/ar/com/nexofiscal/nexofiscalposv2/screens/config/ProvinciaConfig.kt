package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PaisViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal

fun getProvinciaFieldDescriptors(
    paisViewModel: PaisViewModel
): List<FieldDescriptor<Provincia>> {
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
                    label = "Nombre de la Provincia",
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
            id = "pais",
            label = "País",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }

                // CAMBIO: Se reemplaza DropdownSelectionField por el control personalizado.
                EntitySelectionButton(
                    label = "País",
                    selectedValue = entity.pais?.nombre,
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                if (showDialog) {
                    val pagedPaises = paisViewModel.pagedPaises.collectAsLazyPagingItems()
                    // CAMBIO: Se utiliza el SelectionModal genérico.
                    SelectionModal(
                        title = "Seleccionar País",
                        pagedItems = pagedPaises,
                        itemContent = { item -> Text(item.nombre ?: "") },
                        onSearch = { paisViewModel.search(it) },
                        onSelect = { seleccion ->
                            // CAMBIO: Se utiliza la lambda de actualización atómica.
                            onUpdate { it.copy(pais = seleccion) }
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            },
            validator = {
                if (it.pais == null) ValidationResult.Invalid("Debe seleccionar un país.")
                else ValidationResult.Valid
            }
        )
    )
}