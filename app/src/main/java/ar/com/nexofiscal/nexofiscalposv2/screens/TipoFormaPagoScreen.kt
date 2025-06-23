// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/TipoFormaPagoScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoFormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getTipoFormaPagoFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun TipoFormaPagoScreen(
    viewModel: TipoFormaPagoViewModel,
    onDismiss: () -> Unit
) {
    val pagedItems = viewModel.pagedTiposFormaPago.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getTipoFormaPagoFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<TipoFormaPago?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (TipoFormaPago) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Tipos de Forma de Pago",
            items = pagedItems,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = TipoFormaPago()
                showEditScreen = true
            },
            onAttemptEdit = { item ->
                isCreateMode = false
                entityInScreen = item
                showEditScreen = true
            },
            onDelete = { item ->
                viewModel.delete(item.toEntity())
                NotificationManager.show("Tipo de Forma de Pago '${item.nombre}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Tipo de Forma de Pago: $entityLabelText" else "$titlePrefix Tipo de Forma de Pago"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Tipo de Forma de Pago '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}