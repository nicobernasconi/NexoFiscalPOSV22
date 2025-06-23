package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getCategoriaFieldDescriptors(): List<FieldDescriptor<Categoria>> {
    return listOf(
        FieldDescriptor(
            id = "id",
            label = "ID",
            editorContent = { entity, _, _, _ ->
                OutlinedTextField(
                    value = entity.id?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("ID") },
                    readOnly = true,
                    modifier = Modifier
                )
            },
            isReadOnly = { true }
        ),
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { onUpdate(entity.copy(nombre = it)) },
                    label = { Text("Nombre de la Categoría") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    modifier = Modifier
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = entity.seImprime == 1,
                        onCheckedChange = { isChecked ->
                            onUpdate(entity.copy(seImprime = if (isChecked) 1 else 0))
                        },
                        enabled = !isReadOnly
                    )
                    Text("Se imprime en el comprobante")
                }
            }
        )
    )
}