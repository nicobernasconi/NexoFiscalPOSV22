// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/VendedorConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getVendedorFieldDescriptors(): List<FieldDescriptor<Vendedor>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre del Vendedor") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                )
            },
            validator = {
                if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.")
                else ValidationResult.Valid
            }
        ),
        FieldDescriptor(
            id = "direccion",
            label = "Dirección",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.direccion ?: "",
                    onValueChange = { onUpdate(entity.copy(direccion = it)) },
                    label = { Text("Dirección (opcional)") },
                    readOnly = isReadOnly,
                )
            }
        ),
        FieldDescriptor(
            id = "telefono",
            label = "Teléfono",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.telefono ?: "",
                    onValueChange = { onUpdate(entity.copy(telefono = it)) },
                    label = { Text("Teléfono (opcional)") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        ),
        FieldDescriptor(
            id = "porcentajeComision",
            label = "Comisión (%)",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.porcentajeComision?.toString() ?: "",
                    onValueChange = { onUpdate(entity.copy(porcentajeComision = it.toDoubleOrNull())) },
                    label = { Text("Porcentaje de Comisión") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
            }
        )
    )
}