// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/TipoIvaScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getTipoIvaFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun TipoIvaScreen(
    viewModel: TipoIvaViewModel,
    onDismiss: () -> Unit
) {
    val pagedItems = viewModel.pagedTiposIva.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getTipoIvaFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<TipoIVA?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (TipoIVA) -> String = { "${it.nombre} (${it.porcentaje ?: 0.0}%)" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Tipos de IVA",
            items = pagedItems,
            itemContent = { item -> Text("${item.nombre} (${item.porcentaje ?: 0.0}%)") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = TipoIVA()
                showEditScreen = true
            },
            onAttemptEdit = { item ->
                isCreateMode = false
                entityInScreen = item
                showEditScreen = true
            },
             onAttemptDelete  = { item ->
                viewModel.delete(item.toEntity())
                NotificationManager.show("Tipo de IVA '${item.nombre}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.localId }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Tipo de IVA: $entityLabelText" else "$titlePrefix Tipo de IVA"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Tipo de IVA '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}