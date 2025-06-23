package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getTipoIvaFieldDescriptors(): List<FieldDescriptor<TipoIVA>> {
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
                    label = { Text("Nombre del Tipo de IVA") },
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
            id = "letraFactura",
            label = "Letra de Factura",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.letraFactura ?: "",
                    onValueChange = { onUpdate(entity.copy(letraFactura = it)) },
                    label = { Text("Letra de Factura (ej: A, B, C)") },
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
                    value = entity.porcentaje?.toString() ?: "",
                    onValueChange = { onUpdate(entity.copy(porcentaje = it.toDoubleOrNull())) },
                    label = { Text("Porcentaje (ej: 21.0)") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                )
            }
        )
    )
}