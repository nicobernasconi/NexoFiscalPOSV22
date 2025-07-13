// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/TasaIvaScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TasaIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getTasaIvaFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun TasaIvaScreen(
    viewModel: TasaIvaViewModel,
    onDismiss: () -> Unit
) {
    val pagedTasasIva = viewModel.pagedTasasIva.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getTasaIvaFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<TasaIva?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (TasaIva) -> String = { "${it.nombre} (${it.tasa}%)" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Tasas de IVA",
            items = pagedTasasIva,
            itemContent = { item -> Text("${item.nombre} (${item.tasa}%)") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = TasaIva()
                showEditScreen = true
            },
            onAttemptEdit = { tasaIva ->
                isCreateMode = false
                entityInScreen = tasaIva
                showEditScreen = true
            },
             onAttemptDelete  = { tasaIva ->
                viewModel.delete(tasaIva.toEntity())
                NotificationManager.show("Tasa de IVA '${tasaIva.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Tasa de IVA: $entityLabelText" else "$titlePrefix Tasa de IVA"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Tasa de IVA '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}