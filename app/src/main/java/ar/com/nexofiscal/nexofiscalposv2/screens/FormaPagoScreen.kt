// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/FormaPagoScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.FormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoFormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getFormaPagoFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@Composable
fun FormaPagoScreen(
    formaPagoViewModel: FormaPagoViewModel,
    tipoFormaPagoViewModel: TipoFormaPagoViewModel,
    onDismiss: () -> Unit
) {
    val pagedFormasPago = formaPagoViewModel.pagedFormasPago.collectAsLazyPagingItems()
    val fieldDescriptors = remember { getFormaPagoFieldDescriptors(tipoFormaPagoViewModel) }

    var showEditScreen by remember { mutableStateOf(false) }
    var entityInScreen by remember { mutableStateOf<FormaPago?>(null) }
    var isCreateMode by remember { mutableStateOf(false) }

    val itemLabel: (FormaPago) -> String = { "${it.nombre} (${it.porcentaje}%)" }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Gestión de Formas de Pago",
            items = pagedFormasPago,
            itemContent = { item -> Text("${item.nombre} (${item.porcentaje}%)") },
            onSearchQueryChanged = { query -> formaPagoViewModel.search(query) },
            onSelect = { /* No acción */ },
            onDismiss = onDismiss,
            screenMode = CrudScreenMode.EDIT_DELETE,
            onCreate = {
                isCreateMode = true
                entityInScreen = FormaPago()
                showEditScreen = true
            },
            onAttemptEdit = { formaPago ->
                isCreateMode = false
                entityInScreen = formaPago
                showEditScreen = true
            },
             onAttemptDelete  = { formaPago ->
                formaPagoViewModel.eliminar(formaPago.toEntity())
                NotificationManager.show("Forma de Pago '${formaPago.nombre}' eliminada.", NotificationType.SUCCESS)
            },
            itemKey = { it.localId }
        )

        if (showEditScreen && entityInScreen != null) {
            val titlePrefix = if (isCreateMode) "Crear" else "Editar"
            val entityLabelText = if (isCreateMode) "" else itemLabel(entityInScreen!!)
            val dialogTitle = if(entityLabelText.isNotBlank()) "$titlePrefix Forma de Pago: $entityLabelText" else "$titlePrefix Forma de Pago"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = dialogTitle,
                    initialEntity = entityInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedEntity ->
                        formaPagoViewModel.guardar(updatedEntity.toEntity())
                        showEditScreen = false
                        val action = if (isCreateMode) "creada" else "guardada"
                        NotificationManager.show("Forma de Pago '${updatedEntity.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showEditScreen = false }
                )
            }
        }
    }
}