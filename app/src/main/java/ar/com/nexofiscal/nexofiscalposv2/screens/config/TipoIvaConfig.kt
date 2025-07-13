package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getTipoIvaFieldDescriptors(): List<FieldDescriptor<TipoIVA>> {
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
                    label = "Nombre del Tipo de IVA",
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
            id = "letraFactura",
            label = "Letra de Factura",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.letraFactura ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(letraFactura = newValue) } },
                    label = "Letra de Factura (ej: A, B, C)",
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
                    value = entity.porcentaje?.toString() ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(porcentaje = newValue.toDoubleOrNull()) } },
                    label = "Porcentaje (ej: 21.0)",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        )
    )
}