package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme // Importado para el color de error
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.TipoDocumento // Asegúrate de importar los modelos necesarios
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import java.util.regex.Pattern

// Definición de Composable para el editor de Dropdown (placeholder)
@Composable
fun <T> DropdownPlaceholderEditor(
    label: String,
    currentValue: String,
    isReadOnly: Boolean,
    error: String?,
    onClick: () -> Unit // Placeholder para la acción de abrir el dropdown
) {
    OutlinedTextField(
        value = currentValue,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReadOnly, onClick = onClick),
        label = { Text(label) },
        readOnly = true, // El texto no se edita directamente
        isError = error != null,
        supportingText = {
            if (error != null) Text(error)
            else Text("Seleccionar de una lista (implementación pendiente)")
        },
        trailingIcon = {
            // Podrías poner un Icon(Icons.Filled.ArrowDropDown, "Abrir lista")
        }
    )
}


fun getMainClientFieldDescriptors(
    // Parámetros para obtener listas para los dropdowns (necesitarías pasar ViewModels o lambdas)
    // Ejemplo:
    // onFetchTiposDocumento: () -> List<TipoDocumento> = { emptyList() },
    // onFetchTiposIva: () -> List<TipoIVA> = { emptyList() }
): List<FieldDescriptor<Cliente>> {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    return listOf(
        FieldDescriptor(
            id = "nombreCliente",
            label = "Nombre del Cliente",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.nombre) { mutableStateOf(entity.nombre ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate( entity.copy(nombre = it.trim()))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre Completo") },
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { entity ->
                if (entity.nombre.isNullOrBlank()) {
                    ValidationResult.Invalid("El nombre del cliente es obligatorio.")
                } else if (entity.nombre!!.length < 3) {
                    ValidationResult.Invalid("El nombre debe tener al menos 3 caracteres.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "cuitCliente",
            label = "CUIT",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.cuit) { mutableStateOf(entity.cuit ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(cuit = it.filter { char -> char.isDigit() }))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CUIT (solo números)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { entity ->
                val cuit = entity.cuit
                if (!cuit.isNullOrBlank() && (cuit.length != 11 || !cuit.all { it.isDigit() })) {
                    ValidationResult.Invalid("El CUIT debe tener 11 dígitos numéricos.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        // --- Campo Tipo de Documento (Placeholder para Dropdown) ---
        FieldDescriptor(
            id = "tipoDocumentoCliente",
            label = "Tipo de Documento",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // Esto es un placeholder. Idealmente, usarías un DropdownMenuBox
                // que se popule con una lista de Tipos de Documento.
                // Por ahora, muestra el nombre si existe, o un ID editable.
                var currentDisplayValue by remember(entity.tipoDocumento) {
                    mutableStateOf(entity.tipoDocumento?.nombre ?: "ID: ${entity.tipoDocumento?.id ?: "No asignado"}")
                }

              DropdownPlaceholderEditor<TipoDocumento>(
                        label = "Tipo de Documento",
                        currentValue = currentDisplayValue,
                        isReadOnly = isReadOnly,
                        error = error,

                        onClick = {
                            // Aquí se abriría el diálogo/dropdown para seleccionar TipoDocumento
                            // y luego llamarías a onUpdate con el Cliente modificado.
                            // Ejemplo: onUpdate(entity.copy(tipoDocumento = nuevoTipoSeleccionado))
                        }
                    )
                // Si quieres permitir editar el ID directamente (solución temporal):
                // var idValue by remember(entity.tipoDocumento?.id) { mutableStateOf(entity.tipoDocumento?.id?.toString() ?: "") }
                // OutlinedTextField(
                //     value = idValue,
                //     onValueChange = { newValue ->
                //         idValue = newValue
                //         val newId = newValue.toIntOrNull()
                //         val updatedTipoDoc = entity.tipoDocumento?.copy(id = newId ?: 0) ?: TipoDocumento().apply { id = newId ?: 0 }
                //         if (newId != null) { // Solo actualiza si es un ID válido
                //             onUpdate(entity.copy(tipoDocumento = updatedTipoDoc))
                //         } else {
                //              onUpdate(entity.copy(tipoDocumento = null)) // o manejar el error
                //         }
                //     },
                //     label = { Text("ID Tipo Documento (temporal)") },
                //     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                //     modifier = Modifier.fillMaxWidth(),
                //     readOnly = isReadOnly,
                //     isError = error != null,
                //     supportingText = { if (error != null) Text(error) }
                // )
            },
            validator = { entity ->
                if (entity.tipoDocumento == null || entity.tipoDocumento?.id == 0) { // Asumiendo que ID 0 no es válido
                    // ValidationResult.Invalid("Debe seleccionar un tipo de documento.")
                    ValidationResult.Valid // Por ahora, lo hacemos opcional
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "numeroDocumentoCliente",
            label = "Número de Documento",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.numeroDocumento) { mutableStateOf(entity.numeroDocumento ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        // Permite cualquier caracter para Nro Doc, la validación específica puede ser más compleja.
                        onUpdate(entity.copy(numeroDocumento = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nro. Documento") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // Puede ser alfanumérico
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { entity ->
                if (entity.tipoDocumento != null && entity.numeroDocumento.isNullOrBlank()){
                    ValidationResult.Invalid("El número de documento es obligatorio si se seleccionó un tipo.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        // --- Campo Tipo de IVA (Placeholder para Dropdown) ---
        FieldDescriptor(
            id = "tipoIvaCliente",
            label = "Condición de IVA",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var currentDisplayValue by remember(entity.tipoIva) {
                    mutableStateOf(entity.tipoIva?.nombre ?: "ID: ${entity.tipoIva?.id ?: "No asignado"}")
                }
                DropdownPlaceholderEditor<TipoIVA>(
                            label = "Condición de IVA",
                            currentValue = currentDisplayValue,
                            isReadOnly = isReadOnly,
                            error = error,
                            onClick = {

                                // onUpdate(entity.copy(tipoIva = nuevoTipoIvaSeleccionado))
                            }
                        )

                 var idValue by remember(entity.tipoIva?.id) { mutableStateOf(entity.tipoIva?.id?.toString() ?: "") }
                 OutlinedTextField(
                     value = idValue,
                     onValueChange = { newValue ->
                         idValue = newValue
                         val newId = newValue.toIntOrNull()
                         val updatedTipoIva = entity.tipoIva?.copy(id = newId ?: 0) ?: TipoIVA().apply { id = newId ?: 0 }
                         if (newId != null) {
                             onUpdate(entity.copy(tipoIva = updatedTipoIva))
                         } else {
                             onUpdate(entity.copy(tipoIva = null))
                         }
                     },
                     label = { Text("ID Tipo IVA (temporal)") },
                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                     modifier = Modifier.fillMaxWidth(),
                     readOnly = isReadOnly,
                     isError = error != null,
                     supportingText = { if (error != null) Text(error) }
                 )
            },
            validator = { entity ->
                if (entity.tipoIva == null || entity.tipoIva?.id == 0) {
                    // ValidationResult.Invalid("Debe seleccionar una condición de IVA.")
                    ValidationResult.Valid // Por ahora, opcional
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "domicilioComercialCliente",
            label = "Domicilio Comercial",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.direccionComercial) { mutableStateOf(entity.direccionComercial ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(direccionComercial = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Domicilio Comercial") },
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "direccionEntregaCliente",
            label = "Dirección de Entrega",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.direccionEntrega) { mutableStateOf(entity.direccionEntrega ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(direccionEntrega = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dirección de Entrega (opcional)") },
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "telefonoCliente",
            label = "Teléfono",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.telefono) { mutableStateOf(entity.telefono ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(telefono = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "celularCliente",
            label = "Celular",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.celular) { mutableStateOf(entity.celular ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(celular = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Celular (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "emailCliente",
            label = "Email",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var textValue by remember(entity.email) { mutableStateOf(entity.email ?: "") }
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onUpdate(entity.copy(email = it.trim()))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    readOnly = isReadOnly,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    singleLine = true
                )
            },
            validator = { entity ->
                if (entity.email.isNullOrBlank() || emailPattern.matcher(entity.email!!).matches()) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Invalid("El formato del email no es válido.")
                }
            }
        ),
        FieldDescriptor(
            id = "desactivadoCliente",
            label = "Estado del Cliente",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var checkedState by remember(entity.desactivado) { mutableStateOf(entity.desactivado ?: false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !isReadOnly) {
                            if (!isReadOnly) {
                                checkedState = !checkedState
                                onUpdate(entity.copy(desactivado = checkedState))
                            }
                        }
                ) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = {
                            if (!isReadOnly) {
                                checkedState = it
                                onUpdate(entity.copy(desactivado = it))
                            }
                        },
                        enabled = !isReadOnly
                    )
                    Text(
                        text = if (checkedState) "Cliente Desactivado" else "Cliente Activo",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 40.dp) // Alineado con el texto del checkbox
                    )
                }
            },
            validator = { ValidationResult.Valid }
        )
    )
}