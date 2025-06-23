// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/TasaIvaConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getTasaIvaFieldDescriptors(): List<FieldDescriptor<TasaIva>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Tasa") },
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
            id = "tasa",
            label = "Tasa (%)",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.tasa.toString(),
                    onValueChange = { onUpdate(entity.copy(tasa = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Valor de la Tasa") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
            }
        )
    )
}