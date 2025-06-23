// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/TipoDocumentoScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoDocumentoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.TipoDocumento
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getTipoDocumentoFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun TipoDocumentoScreen(
    viewModel: TipoDocumentoViewModel,
    onDismiss: () -> Unit
) {
    val pagedTiposDocumento = viewModel.pagedTiposDocumento.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getTipoDocumentoFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<TipoDocumento?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (TipoDocumento) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Tipos de Documento",
            items = pagedTiposDocumento,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No se necesita acción al seleccionar */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = TipoDocumento()
                showEditScreen = true
            },
            onAttemptEdit = { tipo ->
                isCreateMode = false
                entityInScreen = tipo
                showEditScreen = true
            },
            onDelete = { tipo ->
                viewModel.delete(tipo.toEntity())
                NotificationManager.show("Tipo de Documento '${tipo.nombre}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Tipo de Documento: $entityLabelText" else "$titlePrefix Tipo de Documento"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Tipo de Documento '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}