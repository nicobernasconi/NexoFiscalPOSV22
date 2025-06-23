// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/CategoriaScreen.kt
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
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getCategoriaFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun CategoriaScreen(
    viewModel: CategoriaViewModel,
    onDismiss: () -> Unit
) {
    val pagedCategorias = viewModel.pagedCategorias.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getCategoriaFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Categoria?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Categoria) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Categorías",
            items = pagedCategorias,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Categoria()
                showEditScreen = true
            },
            onAttemptEdit = { categoria ->
                isCreateMode = false
                entityInScreen = categoria
                showEditScreen = true
            },
            onDelete = { categoria ->
                viewModel.remove(categoria.toEntity())
                NotificationManager.show("Categoría '${categoria.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id ?: 0 },
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Categoría: $entityLabelText" else "$titlePrefix Categoría"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.addOrUpdate(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Categoría '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}