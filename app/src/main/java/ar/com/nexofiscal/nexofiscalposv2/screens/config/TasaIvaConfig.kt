package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getTasaIvaFieldDescriptors(): List<FieldDescriptor<TasaIva>> {
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
                    label = "Nombre de la Tasa",
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
            id = "tasa",
            label = "Tasa (%)",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.tasa.toString(),
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(tasa = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Valor de la Tasa",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),

                )
            }
        )
    )
}