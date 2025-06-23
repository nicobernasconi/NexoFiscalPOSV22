package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import ar.com.nexofiscal.nexofiscalposv2.models.Unidad
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getUnidadFieldDescriptors(): List<FieldDescriptor<Unidad>> {
    return listOf(
        FieldDescriptor(
            id = "id",
            label = "ID",
            editorContent = { entity, _, _, _ ->
                OutlinedTextField(
                    value = entity.id.toString(),
                    onValueChange = {},
                    label = { Text("ID") },
                    readOnly = true,
                    modifier = Modifier
                )
            },
            isReadOnly = { true }
        ),
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Unidad") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    modifier = Modifier
                )
            },
            validator = { entity ->
                if (entity.nombre.isNullOrBlank()) {
                    ValidationResult.Invalid("El nombre es obligatorio.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "simbolo",
            label = "Símbolo",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.simbolo ?: "",
                    onValueChange = { onUpdate(entity.copy(simbolo = it)) },
                    label = { Text("Símbolo (ej: Kg, Un, L)") },
                    readOnly = isReadOnly,
                    modifier = Modifier
                )
            }
        )
    )
}