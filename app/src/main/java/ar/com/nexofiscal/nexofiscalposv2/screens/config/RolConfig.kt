// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/RolConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import ar.com.nexofiscal.nexofiscalposv2.models.Rol
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getRolFieldDescriptors(): List<FieldDescriptor<Rol>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre del Rol") },
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
            id = "descripcion",
            label = "Descripción",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.descripcion ?: "",
                    onValueChange = { onUpdate(entity.copy(descripcion = it)) },
                    label = { Text("Descripción") },
                    readOnly = isReadOnly,
                )
            }
        )
    )
}