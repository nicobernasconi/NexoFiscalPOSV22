package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getMonedaFieldDescriptors(): List<FieldDescriptor<Moneda>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.nombre ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(nombre = newValue) } },
                    label = "Nombre de la Moneda",
                    isReadOnly = isReadOnly,
                    error = error
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
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.simbolo ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(simbolo = newValue) } },
                    label = "Símbolo (ej: $, U\$S)",
                    isReadOnly = isReadOnly,
                    error = error
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
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.cotizacion.toString(),
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(cotizacion = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Cotización",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        )
    )
}