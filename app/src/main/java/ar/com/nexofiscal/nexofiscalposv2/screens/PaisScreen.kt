// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/PaisScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PaisViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getPaisFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun PaisScreen(
    viewModel: PaisViewModel,
    onDismiss: () -> Unit
) {
    val pagedPaises = viewModel.pagedPaises.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getPaisFieldDescriptors() }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Pais?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Pais) -> String = { it.nombre ?: "Sin nombre" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Países",
            items = pagedPaises,
            itemContent = { item -> Text(item.nombre ?: "Sin nombre") },
            onSearchQueryChanged = { query -> viewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Pais()
                showEditScreen = true
            },
            onAttemptEdit = { pais ->
                isCreateMode = false
                entityInScreen = pais
                showEditScreen = true
            },
             onAttemptDelete  = { pais ->
                viewModel.delete(pais.toEntity())
                NotificationManager.show("País '${pais.nombre}' eliminado.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix País: $entityLabelText" else "$titlePrefix País"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        viewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creado" else "guardado"
                        NotificationManager.show("País '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}