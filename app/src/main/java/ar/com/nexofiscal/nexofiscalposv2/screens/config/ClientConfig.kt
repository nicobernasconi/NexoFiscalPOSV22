package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal
import java.util.regex.Pattern

fun getMainClientFieldDescriptors(
    tipoDocumentoViewModel: TipoDocumentoViewModel,
    tipoIvaViewModel: TipoIvaViewModel,
    localidadViewModel: LocalidadViewModel,
    provinciaViewModel: ProvinciaViewModel,
    categoriaViewModel: CategoriaViewModel,
    vendedorViewModel: VendedorViewModel
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
            id = "nombre",
            label = "Nombre del Cliente",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(nombre = newValue.trim()) } },
                    label = "Nombre Completo",
                    isReadOnly = isReadOnly,
                    error = error
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
            id = "tipoIva",
            label = "Condición de IVA",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Condición de IVA", entity.tipoIva?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = tipoIvaViewModel.pagedTiposIva.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Condición de IVA", pagedItems, { Text(it.nombre ?: "") }, { tipoIvaViewModel.search(it) }, { seleccion -> onUpdate { it.copy(tipoIva = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.tipoIva == null) ValidationResult.Invalid("Debe seleccionar una condición de IVA.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "cuit",
            label = "CUIT",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.cuit ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(cuit = newValue.filter { char -> char.isDigit() }) } },
                    label = "CUIT (solo números)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isReadOnly = isReadOnly,
                    error = error
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
        FieldDescriptor(
            id = "tipoDocumento",
            label = "Tipo de Documento",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Tipo de Documento", entity.tipoDocumento?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = tipoDocumentoViewModel.pagedTiposDocumento.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Tipo de Documento", pagedItems, { Text(it.nombre ?: "") }, { tipoDocumentoViewModel.search(it) }, { seleccion -> onUpdate { it.copy(tipoDocumento = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { entity ->
                if (entity.cuit.isNullOrBlank() && entity.tipoDocumento == null) {
                    ValidationResult.Invalid("Debe especificar un CUIT o un Tipo de Documento.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "numeroDocumento",
            label = "Número de Documento",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.numeroDocumento ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(numeroDocumento = newValue) } },
                    label = "Nro. Documento",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
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
        FieldDescriptor(
            id = "direccionComercial",
            label = "Domicilio Comercial",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.direccionComercial ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(direccionComercial = newValue) } },
                    label = "Domicilio Comercial",
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        ),
        FieldDescriptor(
            id = "email",
            label = "Email",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.email ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(email = newValue.trim()) } },
                    label = "Email (opcional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isReadOnly = isReadOnly,
                    error = error
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
            id = "localidad",
            label = "Localidad",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Localidad", entity.localidad?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = localidadViewModel.pagedLocalidades.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Localidad", pagedItems, { Text(it.nombre ?: "") }, { localidadViewModel.search(it) }, { seleccion -> onUpdate { it.copy(localidad = seleccion) }; showDialog = false }, { showDialog = false })
                }
            }
        ),
        FieldDescriptor(
            id = "telefono",
            label = "Teléfono",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.telefono ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(telefono = newValue) } },
                    label = "Teléfono (opcional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        )
    )
}