// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/ProvinciaScreen.kt
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
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProvinciaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getProvinciaFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun ProvinciaScreen(
    provinciaViewModel: ProvinciaViewModel,
    paisViewModel: PaisViewModel,
    onDismiss: () -> Unit
) {
    val pagedProvincias = provinciaViewModel.pagedProvincias.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getProvinciaFieldDescriptors(paisViewModel) }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<Provincia?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (Provincia) -> String = { "${it.nombre} (${it.pais?.nombre ?: "S/País"})" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Provincias",
            items = pagedProvincias,
            itemContent = { item -> Text("${item.nombre} (${item.pais?.nombre ?: "S/País"})") },
            onSearchQueryChanged = { query -> provinciaViewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = Provincia()
                showEditScreen = true
            },
            onAttemptEdit = { provincia ->
                isCreateMode = false
                entityInScreen = provincia
                showEditScreen = true
            },
             onAttemptDelete  = { provincia ->
                provinciaViewModel.delete(provincia.toEntity())
                NotificationManager.show("Provincia '${provincia.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.id }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Provincia: $entityLabelText" else "$titlePrefix Provincia"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        provinciaViewModel.save(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Provincia '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}