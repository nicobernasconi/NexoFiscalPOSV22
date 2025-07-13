package ar.com.nexofiscal.nexofiscalposv2.screens.config

import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getPaisFieldDescriptors(): List<FieldDescriptor<Pais>> {
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
                    label = "Nombre del País",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = {
                if (it.nombre.isNullOrBlank()) ValidationResult.Invalid("El nombre es obligatorio.")
                else ValidationResult.Valid
            }
        )
    )
}