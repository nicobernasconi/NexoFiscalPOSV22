package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CategoriaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.LocalidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

fun getProveedorFieldDescriptors(
    localidadViewModel: LocalidadViewModel,
    tipoIvaViewModel: TipoIvaViewModel,
    categoriaViewModel: CategoriaViewModel
): List<FieldDescriptor<Proveedor>> {
    return listOf(
        FieldDescriptor(
            id = "id",
            label = "ID",
            editorContent = { entity, _, _, _ ->
                OutlinedTextField(
                    value = entity.id.toString(),
                    onValueChange = {},
                    label = { Text("ID") },
                    readOnly = true,
                    modifier = Modifier
                )
            },
            isReadOnly = { true }
        ),
        FieldDescriptor(
            id = "razonSocial",
            label = "Razón Social",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.razonSocial ?: "",
                    onValueChange = { onUpdate(entity.copy(razonSocial = it)) },
                    label = { Text("Razón Social") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly
                )
            },
            validator = { if (it.razonSocial.isNullOrBlank()) ValidationResult.Invalid("La Razón Social es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "cuit",
            label = "CUIT",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                OutlinedTextField(
                    value = entity.cuit ?: "",
                    onValueChange = { onUpdate(entity.copy(cuit = it.filter { char -> char.isDigit() })) },
                    label = { Text("CUIT (sin guiones)") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            validator = {
                val cuit = it.cuit
                if (!cuit.isNullOrBlank() && (cuit.length != 11 || !cuit.all { c -> c.isDigit() })) {
                    ValidationResult.Invalid("El CUIT debe tener 11 dígitos.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),

        FieldDescriptor(
            id = "direccion",
            label = "Dirección",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.direccion ?: "",
                    onValueChange = { onUpdate(entity.copy(direccion = it)) },
                    label = { Text("Dirección") },
                    readOnly = isReadOnly
                )
            }
        ),


        FieldDescriptor(
            id = "telefono",
            label = "Teléfono",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.telefono ?: "",
                    onValueChange = { onUpdate(entity.copy(telefono = it)) },
                    label = { Text("Teléfono") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        ),
        FieldDescriptor(
            id = "email",
            label = "Email",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.email ?: "",
                    onValueChange = { onUpdate(entity.copy(email = it)) },
                    label = { Text("Email") },
                    readOnly = isReadOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        )
    )
}