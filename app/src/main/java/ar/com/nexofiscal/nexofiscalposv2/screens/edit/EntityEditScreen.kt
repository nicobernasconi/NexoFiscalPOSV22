package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.R // Asegúrate de tener estos strings
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> EntityEditScreen( //
    title: String,
    initialEntity: T,
    fieldDescriptors: List<FieldDescriptor<T>>,
    onSave: (T) -> Unit,
    onCancel: () -> Unit
) {
    var editableEntity by remember(initialEntity) { mutableStateOf(initialEntity) }
    val validationErrors = remember { mutableStateMapOf<String, String?>() }

    fun validateAllFields(entityToValidate: T): Boolean {
        var allValid = true
        val newErrors = mutableMapOf<String, String?>()
        fieldDescriptors.forEach { descriptor ->
            when (val result = descriptor.validator(entityToValidate)) {
                is ValidationResult.Invalid -> {
                    newErrors[descriptor.id] = result.errorMessage
                    allValid = false
                }
                ValidationResult.Valid -> {
                    newErrors[descriptor.id] = null
                }
            }
        }
        validationErrors.clear()
        validationErrors.putAll(newErrors.filterValues { it != null })
        return allValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.edit_entity_dialog_cancel)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (validateAllFields(editableEntity)) {
                                onSave(editableEntity)
                            }
                        },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NegroNexo),
                                elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                    ),
                        modifier = Modifier.padding(end = 2.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                       Icon(
                            Icons.Default.Save,
                            contentDescription = stringResource(R.string.edit_entity_dialog_save),
                            tint = Blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Puedes ajustar los colores si es necesario
                    containerColor = AzulNexo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface // El color de fondo de la pantalla dentro del diálogo
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fieldDescriptors, key = { it.id }) { descriptor ->
                FieldEditorComponent( // Renombrado de FieldEditor para evitar conflicto si tienes otro
                    descriptor = descriptor,
                    currentEntity = editableEntity,
                    validationErrors = validationErrors,
                    onEntityChange = { updatedEntity ->
                        editableEntity = updatedEntity
                        when (val result = descriptor.validator(updatedEntity)) {
                            is ValidationResult.Invalid -> validationErrors[descriptor.id] = result.errorMessage
                            ValidationResult.Valid -> validationErrors.remove(descriptor.id)
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun <T> FieldEditorComponent(
    descriptor: FieldDescriptor<T>,
    currentEntity: T,
    validationErrors: SnapshotStateMap<String, String?>,
    onEntityChange: (T) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = descriptor.label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        val isFieldReadOnly = descriptor.isReadOnly(currentEntity)
        val fieldError = validationErrors[descriptor.id]

        Box(modifier = Modifier.padding(start = 0.dp)) { // Removida indentación o ajústala
            descriptor.editorContent(
                currentEntity,
                onEntityChange,
                isFieldReadOnly,
                fieldError
            )
        }
        // Mostrar error si no lo hace el editorContent y no es un campo de solo texto con supportingText
        val isTextFieldLike = descriptor.editorContent.toString().contains("OutlinedTextField") ||
                descriptor.editorContent.toString().contains("TextField")
        val hasSupportingText = descriptor.editorContent.toString().contains("supportingText")

        if (fieldError != null && !(isTextFieldLike && hasSupportingText)) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = fieldError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}