// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/ProvinciaConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PaisViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.screens.PagedSelectionDialog
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getProvinciaFieldDescriptors(
    paisViewModel: PaisViewModel
): List<FieldDescriptor<Provincia>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Provincia") },
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
            id = "pais",
            label = "País",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedPaises = paisViewModel.pagedPaises.collectAsLazyPagingItems()

                DropdownSelectionField(
                    label = "País",
                    currentValue = entity.pais?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )

                if (showDialog) {
                    PagedSelectionDialog<Pais>(
                        showDialog = showDialog,
                        onDismiss = { showDialog = false },
                        title = "Seleccionar País",
                        items = pagedPaises,
                        itemContent = { item -> Text(item.nombre ?: "") },
                        onSearch = { paisViewModel.search(it) },
                        onSelect = {
                            onUpdate(entity.copy(pais = it))
                            showDialog = false
                        }
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