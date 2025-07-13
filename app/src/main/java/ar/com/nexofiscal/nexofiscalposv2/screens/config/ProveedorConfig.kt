package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CategoriaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.LocalidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal
import java.util.regex.Pattern

fun getProveedorFieldDescriptors(
    localidadViewModel: LocalidadViewModel,
    tipoIvaViewModel: TipoIvaViewModel,
    categoriaViewModel: CategoriaViewModel
): List<FieldDescriptor<Proveedor>> {
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
            id = "razonSocial",
            label = "Razón Social",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.razonSocial ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(razonSocial = newValue) } },
                    label = "Razón Social",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { if (it.razonSocial.isNullOrBlank()) ValidationResult.Invalid("La Razón Social es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "cuit",
            label = "CUIT",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.cuit ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(cuit = newValue.filter { char -> char.isDigit() }) } },
                    label = "CUIT (sin guiones)",
                    isReadOnly = isReadOnly,
                    error = error,
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
            validator = { if (it.tipoIva == null) ValidationResult.Invalid("La condición de IVA es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "direccion",
            label = "Dirección",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.direccion ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(direccion = newValue) } },
                    label = "Dirección",
                    isReadOnly = isReadOnly,
                    error = error
                )
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
                    label = "Teléfono",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        ),
        FieldDescriptor(
            id = "email",
            label = "Email",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.email ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(email = newValue) } },
                    label = "Email",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
            id = "categoria",
            label = "Categoría",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Categoría (Opcional)", entity.categoria?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = categoriaViewModel.pagedCategorias.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Categoría", pagedItems, { Text(it.nombre ?: "") }, { categoriaViewModel.search(it) }, { seleccion -> onUpdate { it.copy(categoria = seleccion) }; showDialog = false }, { showDialog = false })
                }
            }
        )
    )
}