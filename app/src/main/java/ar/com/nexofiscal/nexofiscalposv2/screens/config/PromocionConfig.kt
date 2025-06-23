package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getPromocionFieldDescriptors(): List<FieldDescriptor<Promocion>> {
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
                    label = { Text("Nombre de la Promoción") },
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
            id = "descripcion",
            label = "Descripción",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.descripcion ?: "",
                    onValueChange = { onUpdate(entity.copy(descripcion = it)) },
                    label = { Text("Descripción (opcional)") },
                    readOnly = isReadOnly,
                    modifier = Modifier
                )
            }
        ),
        FieldDescriptor(
            id = "porcentaje",
            label = "Porcentaje",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.porcentaje.toString(),
                    onValueChange = { onUpdate(entity.copy(porcentaje = it.toIntOrNull() ?: 0)) },
                    label = { Text("Porcentaje de Descuento") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                )
            },
            validator = {
                if (it.porcentaje < 0) {
                    ValidationResult.Invalid("El porcentaje no puede ser negativo.")
                } else {
                    ValidationResult.Valid
                }
            }
        )
    )
}