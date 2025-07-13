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

// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/UsuarioScreen.kt

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

    // --- INICIO DE LA MODIFICACIÓN ---
    var showEditScreen by remember { mutableStateOf(false) }
    // Observamos el StateFlow del ViewModel
    val entityInScreen by viewModel.usuarioParaEditar.collectAsState()
    var isCreateMode by remember { mutableStateOf(false) }
    // --- FIN DE LA MODIFICACIÓN ---

    val itemLabel: (Usuario) -> String = { "${it.nombreCompleto} (${it.nombreUsuario})" }

    // El `LaunchedEffect` se asegura de que cuando `entityInScreen` se popule,
    // se muestre el diálogo.
    LaunchedEffect(entityInScreen) {
        if (entityInScreen != null) {
            isCreateMode = false
            showEditScreen = true
        }
    }

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
                // Para la creación, podemos instanciar un objeto vacío directamente.
                viewModel.limpiarUsuarioParaEdicion() // Limpiamos por si acaso
                // No llamamos a cargarUsuarioParaEdicion, sino que preparamos un estado de creación
                // Esto se podría manejar dentro del ViewModel si la lógica de creación es compleja.
                // Por ahora, lo manejaremos en la pantalla de edición.
                showEditScreen = true
            },
            onAttemptEdit = { item ->
                // En lugar de mostrar la pantalla directamente, le pedimos al ViewModel que cargue los datos.
                viewModel.cargarUsuarioParaEdicion(item.id)
            },
             onAttemptDelete  = { item ->
                viewModel.delete(item.toEntity())
                NotificationManager.show("Usuario '${item.nombreUsuario}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        // El diálogo de edición ahora se muestra cuando `showEditScreen` es true
        if (showEditScreen) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityToEdit = if (isCreateMode) remember { Usuario() } else entityInScreen

            if (entityToEdit != null) {
                val entityLabelText = if (isCreateMode) "" else itemLabel(entityToEdit)
                val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Usuario: $entityLabelText" else "$titlePrefix Usuario"

                Surface(modifier = Modifier.fillMaxSize()) {
                    EntityEditScreen(
                        title = dialogTitle,
                        initialEntity = entityToEdit,
                        fieldDescriptors = fieldDescriptors,
                        onSave = { updatedEntity ->
                            viewModel.save(updatedEntity.toEntity())
                            showEditScreen = false
                            viewModel.limpiarUsuarioParaEdicion() // Limpiar el estado
                            val action = if (isCreateMode) "creado" else "guardado"
                            NotificationManager.show("Usuario '${updatedEntity.nombreUsuario}' $action.", NotificationType.SUCCESS)
                        },
                        onCancel = {
                            showEditScreen = false
                            viewModel.limpiarUsuarioParaEdicion() // Limpiar el estado
                        }
                    )
                }
            }
        }
    }
}