// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/TipoScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Tipo
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getTipoFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun TipoScreen(
    viewModel: TipoViewModel,
    onDismiss: () -> Unit
) {
    val pagedItems = viewModel.pagedTipos.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getTipoFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Tipo?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Tipo) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Tipos",
            items = pagedItems,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Tipo()
                showEditScreen = true
            },
            onAttemptEdit = { item ->
                isCreateMode = false
                entityInScreen = item
                showEditScreen = true
            },
            onDelete = { item ->
                viewModel.delete(item.toEntity())
                NotificationManager.show("Tipo '${item.nombre}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Tipo: $entityLabelText" else "$titlePrefix Tipo"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Tipo '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}