// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/MonedaScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.MonedaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getMonedaFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun MonedaScreen(
    viewModel: MonedaViewModel,
    onDismiss: () -> Unit
) {
    val pagedMonedas = viewModel.pagedMonedas.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getMonedaFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Moneda?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Moneda) -> String = { "${it.nombre} (${it.simbolo})" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Monedas",
            items = pagedMonedas,
            itemContent = { item -> Text("${item.nombre} (${item.simbolo})") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Moneda()
                showEditScreen = true
            },
            onAttemptEdit = { moneda ->
                isCreateMode = false
                entityInScreen = moneda
                showEditScreen = true
            },
            onDelete = { moneda ->
                viewModel.delete(moneda.toEntity())
                NotificationManager.show("Moneda '${moneda.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Moneda: $entityLabelText" else "$titlePrefix Moneda"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Moneda '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}