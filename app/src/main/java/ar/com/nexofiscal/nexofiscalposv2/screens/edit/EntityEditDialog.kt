package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EntityEditDialog(
    onDismissRequest: () -> Unit,
    dialogTitlePrefix: String = "Editar", // Prefijo para el título del diálogo
    entityLabel: String, // Etiqueta de la entidad para el título, ej. itemLabel(entity)
    initialEntity: T,
    fieldDescriptors: List<FieldDescriptor<T>>,
    onSaveEntity: (T) -> Unit // Callback cuando la entidad se guarda exitosamente
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            val completeTitle = if (entityLabel.isNotBlank()) "$dialogTitlePrefix: $entityLabel" else dialogTitlePrefix

            EntityEditScreen( // Usa la EntityEditScreen interna
                title = completeTitle,
                initialEntity = initialEntity,
                fieldDescriptors = fieldDescriptors,
                onSave = { entity ->
                    onSaveEntity(entity)
                    onDismissRequest() // Cierra el diálogo después de guardar
                },
                onCancel = onDismissRequest
            )
        }
    }
}