package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getPromocionFieldDescriptors(): List<FieldDescriptor<Promocion>> {
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
                    label = "Nombre de la Promoción",
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
            }
        ),
        FieldDescriptor(
            id = "descripcion",
            label = "Descripción",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.descripcion ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(descripcion = newValue) } },
                    label = "Descripción (opcional)",
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        ),
        FieldDescriptor(
            id = "porcentaje",
            label = "Porcentaje",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.porcentaje.toString(),
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(porcentaje = newValue.toIntOrNull() ?: 0) } },
                    label = "Porcentaje de Descuento",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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