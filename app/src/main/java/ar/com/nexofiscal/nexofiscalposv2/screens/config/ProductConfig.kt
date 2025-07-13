package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.CheckboxRow
import ar.com.nexofiscal.nexofiscalposv2.ui.EntitySelectionButton
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectionModal
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

/**
 * Define y configura todos los campos para el formulario de creación y edición de Productos,
 * utilizando los controles personalizados y el patrón de actualización atómica.
 */
fun getMainProductFieldDescriptors(
    tipoViewModel: TipoViewModel,
    familiaViewModel: FamiliaViewModel,
    tasaIvaViewModel: TasaIvaViewModel,
    unidadViewModel: UnidadViewModel,
    proveedorViewModel: ProveedorViewModel,
    agrupacionViewModel: AgrupacionViewModel,
    monedaViewModel: MonedaViewModel
): List<FieldDescriptor<Producto>> {
    return listOf(
        // --- SECCIÓN: IDENTIFICACIÓN PRINCIPAL ---
        FieldDescriptor(
            id = "tipo",
            label = "Tipo",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Tipo de Producto", entity.tipo?.nombre, isReadOnly, null) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = tipoViewModel.pagedTipos.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Tipo", pagedItems, { Text(it.nombre ?: "") }, { tipoViewModel.search(it) }, { seleccion -> onUpdate { it.copy(tipo = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.tipo == null) ValidationResult.Invalid("El tipo de producto es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "codigo",
            label = "Código",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.codigo ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(codigo = newValue) } },
                    label = "Código / PLU",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { if (it.codigo.isNullOrBlank()) ValidationResult.Invalid("El código es obligatorio.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "descripcion",
            label = "Descripción",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.descripcion ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(descripcion = newValue) } },
                    label = "Descripción",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { if (it.descripcion.isNullOrBlank()) ValidationResult.Invalid("La descripción es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "activo",
            label = "Estado",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                CheckboxRow(
                    "Producto Activo",
                    checked = entity.activo == 1,
                    onCheckedChange = { isChecked -> onUpdate { it.copy(activo = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        ),
        FieldDescriptor(
            id = "favorito",
            label = "Favorito",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                CheckboxRow(
                    "Marcar como Favorito (para PLU Directos)",
                    checked = entity.favorito == 1,
                    onCheckedChange = { isChecked -> onUpdate { it.copy(favorito = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        ),
        FieldDescriptor(
            id = "familia",
            label = "Familia",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Familia", entity.familia?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = familiaViewModel.pagedFamilias.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Familia", pagedItems, { Text(it.nombre ?: "") }, { familiaViewModel.search(it) }, { seleccion -> onUpdate { it.copy(familia = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.familia == null) ValidationResult.Invalid("La familia es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "tasaIva",
            label = "Tasa de IVA",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Tasa de IVA", entity.tasaIva?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = tasaIvaViewModel.pagedTasasIva.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Tasa de IVA", pagedItems, { Text("${it.nombre} (${it.tasa}%)") }, { tasaIvaViewModel.search(it) }, { seleccion -> onUpdate { it.copy(tasaIva = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.tasaIva == null) ValidationResult.Invalid("La Tasa de IVA es obligatoria.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "agrupacion",
            label = "Agrupación (PLU)",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Agrupación (PLU)", entity.agrupacion?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = agrupacionViewModel.pagedAgrupaciones.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Agrupación", pagedItems, { Text(it.nombre ?: "") }, { agrupacionViewModel.search(it) }, { seleccion -> onUpdate { it.copy(agrupacion = seleccion) }; showDialog = false }, { showDialog = false })
                }
            }
        ),
        FieldDescriptor(
            id = "unidad",
            label = "Unidad de Medida",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                EntitySelectionButton("Unidad de Medida", entity.unidad?.nombre, isReadOnly, error) { if (!isReadOnly) showDialog = true }
                if (showDialog) {
                    val pagedItems = unidadViewModel.pagedUnidades.collectAsLazyPagingItems()
                    SelectionModal("Seleccionar Unidad", pagedItems, { Text(it.nombre ?: "") }, { unidadViewModel.search(it) }, { seleccion -> onUpdate { it.copy(unidad = seleccion) }; showDialog = false }, { showDialog = false })
                }
            },
            validator = { if (it.unidad == null) ValidationResult.Invalid("La unidad de medida es obligatoria.") else ValidationResult.Valid }
        ),

        // --- SECCIÓN: PRECIOS ---
        FieldDescriptor(
            id = "precio1",
            label = "Precio 1",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.precio1.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(precio1 = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Precio de Venta",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            validator = { if (it.precio1 <= 0) ValidationResult.Invalid("El precio de venta debe ser mayor a 0.") else ValidationResult.Valid }
        ),
        FieldDescriptor(
            id = "precio2",
            label = "Precio 2",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.precio2.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(precio2 = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Precio 2",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        ),
        FieldDescriptor(
            id = "precio3",
            label = "Precio 3",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.precio3.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(precio3 = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Precio 3",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        ),
        FieldDescriptor(
            id = "precioCosto",
            label = "Precio de Costo",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.precioCosto.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(precioCosto = newValue.toDoubleOrNull() ?: 0.0) } },
                    label = "Precio de Costo",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        ),

        // --- SECCIÓN: STOCK ---
        FieldDescriptor(
            id = "stock",
            label = "Stock",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.stock.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(stock = newValue.toIntOrNull() ?: 0) } },
                    label = "Stock Actual",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "stockMinimo",
            label = "Stock Mínimo",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                SelectAllTextField(
                    value = entity.stockMinimo.toString(),
                    onValueChange = { newValue -> onUpdate { it.copy(stockMinimo = newValue.toIntOrNull() ?: 0) } },
                    label = "Stock Mínimo",
                    isReadOnly = isReadOnly, error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),

        // --- SECCIÓN: CONFIGURACIONES ADICIONALES ---
        FieldDescriptor(
            id = "productoBalanza",
            label = "Balanza",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                CheckboxRow(
                    "Es un producto pesado por balanza",
                    checked = entity.productoBalanza == 1,
                    onCheckedChange = { isChecked -> onUpdate { it.copy(productoBalanza = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        ),
        FieldDescriptor(
            id = "rg5329_23",
            label = "RG5329/23",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                CheckboxRow(
                    "Aplica RG5329/23",
                    checked = entity.rg5329_23 == 1,
                    onCheckedChange = { isChecked -> onUpdate { it.copy(rg5329_23 = if (isChecked) 1 else 0) } },
                    isReadOnly = isReadOnly
                )
            }
        ),
    )
}