// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/UsuarioScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.RolViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.SucursalViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.UsuarioViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.VendedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getUsuarioFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun UsuarioScreen(
    viewModel: UsuarioViewModel,
    rolViewModel: RolViewModel,
    sucursalViewModel: SucursalViewModel,
    vendedorViewModel: VendedorViewModel,
    onDismiss: () -> Unit
) {
    val pagedItems = viewModel.pagedUsuarios.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getUsuarioFieldDescriptors(rolViewModel, sucursalViewModel, vendedorViewModel) }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Usuario?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Usuario) -> String = { "${it.nombreCompleto} (${it.nombreUsuario})" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Usuarios",
            items = pagedItems,
            itemContent = { item -> Text("${item.nombreCompleto} (${item.nombreUsuario})" ?: "") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Usuario()
                showEditScreen = true
            },
            onAttemptEdit = { item ->
                isCreateMode = false
                entityInScreen = item
                showEditScreen = true
            },
            onDelete = { item ->
                viewModel.delete(item.toEntity())
                NotificationManager.show("Usuario '${item.nombreUsuario}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Usuario: $entityLabelText" else "$titlePrefix Usuario"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Usuario '${updatedEntity.nombreUsuario}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}