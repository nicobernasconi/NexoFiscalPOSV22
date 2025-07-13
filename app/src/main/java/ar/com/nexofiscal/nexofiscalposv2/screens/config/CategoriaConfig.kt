package ar.com.nexofiscal.nexofiscalposv2.screens.config

import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.CheckboxRow
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getCategoriaFieldDescriptors(): List<FieldDescriptor<Categoria>> {
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
                    label = "Nombre de la Categoría",
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
            id = "seImprime",
            label = "Se Imprime",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                // CAMBIO: Se reemplaza la Row manual por el control reutilizable CheckboxRow.
                CheckboxRow(
                    label = "Se imprime en el comprobante",
                    checked = entity.seImprime == 1,
                    // CAMBIO: Se utiliza la lambda de actualización atómica.
                    onCheckedChange = { isChecked -> onUpdate { it.copy(seImprime = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        )
    )
}