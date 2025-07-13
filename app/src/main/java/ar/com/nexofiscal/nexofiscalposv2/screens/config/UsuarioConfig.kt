package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.RolViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.SucursalViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.VendedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.CheckboxRow
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal

fun getUsuarioFieldDescriptors(
    rolViewModel: RolViewModel,
    sucursalViewModel: SucursalViewModel,
    vendedorViewModel: VendedorViewModel
): List<FieldDescriptor<Usuario>> {
    return listOf(
        FieldDescriptor(
            id = "nombreUsuario",
            label = "Nombre de Usuario",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.nombreUsuario ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(nombreUsuario = newValue) } },
                    label = "Nombre de Usuario",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = {
                if (it.nombreUsuario.isNullOrBlank()) ValidationResult.Invalid("El nombre de usuario es obligatorio.")
                else ValidationResult.Valid
            }
        ),
       FieldDescriptor(
                        id = "password",
                        label = "Contraseña",
                        editorContent = { entity, onUpdate, isReadOnly, error ->
                            SelectAllTextField(
                                value = entity.password ?: "",
                                onValueChange = { newValue -> onUpdate { it.copy(password = newValue) } },
                                label = "Contraseña (dejar en blanco para no cambiar)",
                                isReadOnly = isReadOnly,
                                error = error
                            )
                        }
                    ),
        FieldDescriptor(
            id = "nombreCompleto",
            label = "Nombre Completo",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.nombreCompleto ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(nombreCompleto = newValue) } },
                    label = "Nombre Completo",
                    isReadOnly = isReadOnly,
                    error = error
                )
            }
        ),
        FieldDescriptor(
            id = "rol",
            label = "Rol",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton(
                    label = "Rol",
                    selectedValue = entity.rol?.nombre,
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )
                if (showDialog) {
                    val pagedItems = rolViewModel.pagedRoles.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Rol", pagedItems, { Text(it.nombre ?: "") }, { rolViewModel.search(it) }, { seleccion -> onUpdate { it.copy(rol = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.rol == null) ValidationResult.Invalid("El rol es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "sucursal",
            label = "Sucursal",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton(
                    label = "Sucursal",
                    selectedValue = entity.sucursal?.nombre,
                    isReadOnly = isReadOnly,
                    error = error,
                    onClick = { if (!isReadOnly) showDialog = true }
                )
                if (showDialog) {
                    val pagedItems = sucursalViewModel.pagedSucursales.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Sucursal", pagedItems, { Text(it.nombre ?: "") }, { sucursalViewModel.search(it) }, { seleccion -> onUpdate { it.copy(sucursal = seleccion) }; showDialog = false }, { showDialog = false })
                }
            }
        ),
        FieldDescriptor(
            id = "vendedor",
            label = "Vendedor",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton(
                    label = "Vendedor (opcional)",
                    selectedValue = entity.vendedor?.nombre,
                    isReadOnly = isReadOnly,
                    error = null,
                    onClick = { if (!isReadOnly) showDialog = true }
                )
                if (showDialog) {
                    val pagedItems = vendedorViewModel.pagedVendedores.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Vendedor", pagedItems, { Text(it.nombre ?: "") }, { vendedorViewModel.search(it) }, { seleccion -> onUpdate { it.copy(vendedor = seleccion) }; showDialog = false }, { showDialog = false })
                }
            }
        ),
        FieldDescriptor(
            id = "activo",
            label = "Estado",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                CheckboxRow(
                    "Usuario Activo",
                    checked = entity.activo == 1,
                    onCheckedChange = { isChecked -> onUpdate { it.copy(activo = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        )
    )
}