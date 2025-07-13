package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getVendedorFieldDescriptors(): List<FieldDescriptor<Vendedor>> {
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
                    label = "Nombre del Vendedor",
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
            id = "direccion",
            label = "Dirección",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.direccion ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(direccion = newValue) } },
                    label = "Dirección (opcional)",
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        ),
        FieldDescriptor(
            id = "telefono",
            label = "Teléfono",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.telefono ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(telefono = newValue) } },
                    label = "Teléfono (opcional)",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        ),
        FieldDescriptor(
            id = "porcentajeComision",
            label = "Comisión (%)",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.porcentajeComision?.toString() ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(porcentajeComision = newValue.toDoubleOrNull()) } },
                    label = "Porcentaje de Comisión",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        )
    )
}