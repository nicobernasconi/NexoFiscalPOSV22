package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getAgrupacionFieldDescriptors(): List<FieldDescriptor<Agrupacion>> {
    return listOf(
        FieldDescriptor(
            id = "id",
            label = "ID",
            editorContent = { entity, _, isReadOnly, _ ->
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
                    label = { Text("Nombre de la Agrupación") },
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
            id = "numero",
            label = "Número",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.numero?.toString() ?: "",
                    onValueChange = { onUpdate(entity.copy(numero = it.toIntOrNull())) },
                    label = { Text("Número (opcional)") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                )
            }
        ),
        FieldDescriptor(
            id = "color",
            label = "Color",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.color ?: "",
                    onValueChange = { onUpdate(entity.copy(color = it)) },
                    label = { Text("Color (Ej: #FF0000)") },
                    readOnly = isReadOnly,
                    modifier = Modifier
                )
            }
        ),
        FieldDescriptor(
            id = "icono",
            label = "Ícono",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.icono ?: "",
                    onValueChange = { onUpdate(entity.copy(icono = it)) },
                    label = { Text("Ícono (opcional)") },
                    readOnly = isReadOnly,
                    modifier = Modifier
                )
            }
        )
    )
}