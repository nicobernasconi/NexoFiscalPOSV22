// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/TipoFormaPagoConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getTipoFormaPagoFieldDescriptors(): List<FieldDescriptor<TipoFormaPago>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre del Tipo de Forma de Pago") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                )
            },
            validator = {
                if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.")
                else ValidationResult.Valid
            }
        )
    )
}