package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Familia
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getFamiliaFieldDescriptors(): List<FieldDescriptor<Familia>> {
    return listOf(FieldDescriptor(
        id = "numero",
        label = "Número",
        editorContent = { entity, onUpdate, isReadOnly, error ->
            // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
            SelectAllTextField(
                value = entity.numero?.toString() ?: "",
                // CAMBIO: Se utiliza la lambda de actualización atómica.
                onValueChange = { newValue -> onUpdate { it.copy(numero = newValue.toIntOrNull()) } },
                label = "Número",
                isReadOnly = isReadOnly,
                error = error,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    ),
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.nombre ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(nombre = newValue) } },
                    label = "Nombre de la Familia",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { entity ->
                if (entity.nombre.isNullOrBlank()) {
                    ValidationResult.Invalid("El nombre es obligatorio.")
                } else {
                    ValidationResult.Valid
                }
                if (entity.numero == null) {
                    ValidationResult.Invalid("El número es obligatorio.")
                } else {
                    ValidationResult.Valid
                }
            }
        )

    )
}