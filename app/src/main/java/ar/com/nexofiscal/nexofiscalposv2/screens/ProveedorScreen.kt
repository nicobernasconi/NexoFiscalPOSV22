// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/ProveedorScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CategoriaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.LocalidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProveedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getProveedorFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun ProveedorScreen(
    proveedorViewModel: ProveedorViewModel,
    localidadViewModel: LocalidadViewModel,
    tipoIvaViewModel: TipoIvaViewModel,
    categoriaViewModel: CategoriaViewModel,
    onDismiss: () -> Unit
) {
    val pagedProveedores = proveedorViewModel.pagedProveedores.collectAsLazyPagingItems()
    val fieldDescriptors = remember {
        getProveedorFieldDescriptors(localidadViewModel, tipoIvaViewModel, categoriaViewModel)
    }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Proveedor?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Proveedor) -> String = { "${it.razonSocial} (${it.cuit ?: "Sin CUIT"})" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Proveedores",
            items = pagedProveedores,
            itemContent = { item -> Text("${item.razonSocial} (${item.cuit ?: "Sin CUIT"})") },
            onSearchQueryChanged = { query -> proveedorViewModel.search(query) },
            onSelect = { /* Sin acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Proveedor()
                showEditScreen = true
            },
            onAttemptEdit = { proveedor ->
                isCreateMode = false
                entityInScreen = proveedor
                showEditScreen = true
            },
             onAttemptDelete  = { proveedor ->
                proveedorViewModel.delete(proveedor.toEntity())
                NotificationManager.show("Proveedor '${proveedor.razonSocial}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.localId }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Proveedor: $entityLabelText" else "$titlePrefix Proveedor"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        proveedorViewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("Proveedor '${updatedEntity.razonSocial}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}