// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/config/UsuarioConfig.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.RolViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.SucursalViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.VendedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Rol
import ar.com.nexofiscal.nexofiscalposv2.models.Sucursal
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import ar.com.nexofiscal.nexofiscalposv2.screens.PagedSelectionDialog
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult

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
                OutlinedTextField(
                    value = entity.nombreUsuario ?: "",
                    onValueChange = { onUpdate(entity.copy(nombreUsuario = it)) },
                    label = { Text("Nombre de Usuario") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    readOnly = isReadOnly,
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
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.password ?: "",
                    onValueChange = { onUpdate(entity.copy(password = it)) },
                    label = { Text("Contraseña (dejar en blanco para no cambiar)") },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isReadOnly,
                )
            }
        ),
        FieldDescriptor(
            id = "nombreCompleto",
            label = "Nombre Completo",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                OutlinedTextField(
                    value = entity.nombreCompleto ?: "",
                    onValueChange = { onUpdate(entity.copy(nombreCompleto = it)) },
                    label = { Text("Nombre Completo") },
                    readOnly = isReadOnly,
                )
            }
        ),
        FieldDescriptor(
            id = "rol",
            label = "Rol",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = rolViewModel.pagedRoles.collectAsLazyPagingItems()

                DropdownSelectionField("Rol", entity.rol?.nombre ?: "Seleccionar...", isReadOnly, error) {
                    if (!isReadOnly) showDialog = true
                }

                PagedSelectionDialog(showDialog, { showDialog = false }, "Seleccionar Rol", pagedItems, { it.nombre ?: "" }, { rolViewModel.search(it) }) {
                    onUpdate(entity.copy(rol = it)); showDialog = false
                }
            },
            validator = { if(it.rol == null) ValidationResult.Invalid("El rol es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "sucursal",
            label = "Sucursal",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = sucursalViewModel.pagedSucursales.collectAsLazyPagingItems()

                DropdownSelectionField("Sucursal", entity.sucursal?.nombre ?: "Seleccionar...", isReadOnly, error) {
                    if (!isReadOnly) showDialog = true
                }

                PagedSelectionDialog(showDialog, { showDialog = false }, "Seleccionar Sucursal", pagedItems, { it.nombre ?: "" }, { sucursalViewModel.search(it) }) {
                    onUpdate(entity.copy(sucursal = it)); showDialog = false
                }
            }
        ),
        FieldDescriptor(
            id = "vendedor",
            label = "Vendedor",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = vendedorViewModel.pagedVendedores.collectAsLazyPagingItems()

                DropdownSelectionField("Vendedor (opcional)", entity.vendedor?.nombre ?: "Seleccionar...", isReadOnly, null) {
                    if (!isReadOnly) showDialog = true
                }

                PagedSelectionDialog(showDialog, { showDialog = false }, "Seleccionar Vendedor", pagedItems, { it.nombre ?: "" }, { vendedorViewModel.search(it) }) {
                    onUpdate(entity.copy(vendedor = it)); showDialog = false
                }
            }
        ),
        FieldDescriptor(
            id = "activo",
            label = "Estado",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(
                        checked = entity.activo == 1,
                        onCheckedChange = { isChecked -> onUpdate(entity.copy(activo = if (isChecked) 1 else 0)) },
                        enabled = !isReadOnly
                    )
                    Text("Usuario Activo")
                }
            }
        )
    )
}