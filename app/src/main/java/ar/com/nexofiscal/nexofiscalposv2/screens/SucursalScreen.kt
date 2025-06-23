// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/SucursalScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.SucursalViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Sucursal
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getSucursalFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun SucursalScreen(
    viewModel: SucursalViewModel,
    onDismiss: () -> Unit
) {
    val pagedSucursales = viewModel.pagedSucursales.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getSucursalFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Sucursal?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Sucursal) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Sucursales",
            items = pagedSucursales,
            itemContent = { item -> Text(item.nombre ?: "") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Sucursal()
                showEditScreen = true
            },
            onAttemptEdit = { sucursal ->
                isCreateMode = false
                entityInScreen = sucursal
                showEditScreen = true
            },
            onDelete = { sucursal ->
                viewModel.delete(sucursal.toEntity())
                NotificationManager.show("Sucursal '${sucursal.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Sucursal: $entityLabelText" else "$titlePrefix Sucursal"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Sucursal '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}