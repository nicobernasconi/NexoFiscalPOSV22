// src/main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/AgrupacionScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.AgrupacionViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudListScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudScreenMode
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun AgrupacionScreen(
    viewModel: AgrupacionViewModel,
    onDismiss: () -> Unit
) {
    val pagedAgrupaciones = viewModel.pagedAgrupaciones.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getAgrupacionFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Agrupacion?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Agrupacion) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Agrupaciones",
            items = pagedAgrupaciones,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Agrupacion()
                showEditScreen = true
            },
            onAttemptEdit = { agrupacion ->
                isCreateMode = false
                entityInScreen = agrupacion
                showEditScreen = true
            },
            onAttemptDelete  = { agrupacion ->
                viewModel.remove(agrupacion.toEntity())
                NotificationManager.show("Agrupación '${agrupacion.nombre}' eliminada.", NotificationType.SUCCESS)
            },

        itemKey = { it.localId }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Agrupación: $entityLabelText" else "$titlePrefix Agrupación"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.addOrUpdate(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Agrupación '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}