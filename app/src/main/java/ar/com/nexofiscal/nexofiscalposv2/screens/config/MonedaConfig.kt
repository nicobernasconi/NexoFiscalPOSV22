// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/MonedaConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getMonedaFieldDescriptors(): List<FieldDescriptor<Moneda>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Moneda") },
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
            id = "simbolo",
            label = "Símbolo",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.simbolo ?: "",
                    onValueChange = { onUpdate(entity.copy(simbolo = it)) },
                    label = { Text("Símbolo (ej: $, U\$S)") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                )
            },
            validator = {
                if (it.simbolo.isNullOrBlank()) ValidationResult.Invalid("El símbolo es obligatorio.")
                else ValidationResult.Valid
            }
        ),
        FieldDescriptor(
            id = "cotizacion",
            label = "Cotización",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.cotizacion.toString(),
                    onValueChange = { onUpdate(entity.copy(cotizacion = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Cotización") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        )
    )
}