// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/PromocionScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PromocionViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getPromocionFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun PromocionScreen(
    viewModel: PromocionViewModel,
    onDismiss: () -> Unit
) {
    val pagedPromociones = viewModel.pagedPromociones.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getPromocionFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Promocion?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Promocion) -> String = { "${it.nombre} (${it.porcentaje}%)" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Promociones",
            items = pagedPromociones,
            itemContent = { item -> Text("${item.nombre} (${item.porcentaje}%)") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* Sin acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Promocion()
                showEditScreen = true
            },
            onAttemptEdit = { promocion ->
                isCreateMode = false
                entityInScreen = promocion
                showEditScreen = true
            },
            onDelete = { promocion ->
                viewModel.delete(promocion.toEntity())
                NotificationManager.show("Promoción '${promocion.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Promoción: $entityLabelText" else "$titlePrefix Promoción"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Promoción '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}