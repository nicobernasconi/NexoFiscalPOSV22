package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.LocalidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProvinciaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getLocalidadFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun LocalidadScreen(
    localidadViewModel: LocalidadViewModel,
    provinciaViewModel: ProvinciaViewModel,
    onDismiss: () -> Unit
) {
    val pagedLocalidades = localidadViewModel.pagedLocalidades.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getLocalidadFieldDescriptors(provinciaViewModel) }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Localidad?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Localidad) -> String = { "${it.nombre} (${it.provincia?.nombre ?: "S/P"})" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Localidades",
            items = pagedLocalidades,
            itemContent = { item -> Text("${item.nombre} (${item.provincia?.nombre ?: "S/P"})") },
            onSearchQueryChanged = { query -> localidadViewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Localidad()
                showEditScreen = true
            },
            onAttemptEdit = { localidad ->
                isCreateMode = false
                entityInScreen = localidad
                showEditScreen = true
            },
            onDelete = { localidad ->
                localidadViewModel.delete(localidad.toEntity())
                NotificationManager.show("Localidad '${localidad.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Localidad: $entityLabelText" else "$titlePrefix Localidad"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        localidadViewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Localidad '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}