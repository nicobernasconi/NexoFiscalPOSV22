package ar.com.nexofiscal.nexofiscalposv2.screens.config

import ar.com.nexofiscal.nexofiscalposv2.models.Unidad
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getUnidadFieldDescriptors(): List<FieldDescriptor<Unidad>> {
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
                    label = "Nombre de la Unidad",
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
            id = "simbolo",
            label = "Símbolo",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // CAMBIO: Se reemplaza OutlinedTextField por el control personalizado.
                SelectAllTextField(
                    value = entity.simbolo ?: "",
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onValueChange = { newValue -> onUpdate { it.copy(simbolo = newValue) } },
                    label = "Símbolo (ej: Kg, Un, L)",
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        )
    )
}