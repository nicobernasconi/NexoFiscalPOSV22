package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.screens.PagedSelectionDialog
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.*

// La función privada SelectionDialog se ha eliminado porque es obsoleta.
// Se usará PagedSelectionDialog en su lugar.

fun getMainProductFieldDescriptors(
    tipoViewModel: TipoViewModel,
    familiaViewModel: FamiliaViewModel,
    tasaIvaViewModel: TasaIvaViewModel,
    unidadViewModel: UnidadViewModel,
    proveedorViewModel: ProveedorViewModel,
    agrupacionViewModel: AgrupacionViewModel
): List<FieldDescriptor<Producto>> {
    val customizations = mapOf<String, FieldCustomization<Producto>>(
        "tipo" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = tipoViewModel.pagedTipos.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<Producto>(
                    label = "Tipo de Producto",
                    currentValueDisplay = entity.tipo?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Tipo",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { tipoViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(tipo = it)); showDialog = false }
                )
            }
        ),

        "familia" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = familiaViewModel.pagedFamilias.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<Producto>(
                    label = "Familia",
                    currentValueDisplay = entity.familia?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Familia",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { familiaViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(familia = it)); showDialog = false }
                )
            }
        ),

        "tasaIva" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = tasaIvaViewModel.pagedTasasIva.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<TasaIva>(
                    label = "Tasa de IVA",
                    currentValueDisplay = entity.tasaIva?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Tasa de IVA",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { tasaIvaViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(tasaIva = it)); showDialog = false }
                )
            }
        ),

        "unidad" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = unidadViewModel.pagedUnidades.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<Unidad>(
                    label = "Unidad de Medida",
                    currentValueDisplay = entity.unidad?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Unidad",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { unidadViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(unidad = it)); showDialog = false }
                )
            }
        ),

        "proveedor" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = proveedorViewModel.pagedProveedores.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<Proveedor>(
                    label = "Proveedor",
                    currentValueDisplay = entity.proveedor?.razonSocial ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Proveedor",
                    items = pagedItems,
                    itemContent = { item -> Text(item.razonSocial ?: "") },
                    onSearch = { proveedorViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(proveedor = it)); showDialog = false }
                )
            }
        ),

        "agrupacion" to FieldCustomization(
            editorContent = { entity, onUpdate, isReadOnly, error ->
                var showDialog by remember { mutableStateOf(false) }
                val pagedItems = agrupacionViewModel.pagedAgrupaciones.collectAsLazyPagingItems()

                DropdownPlaceholderEditor<Agrupacion>(
                    label = "Agrupación",
                    currentValueDisplay = entity.agrupacion?.nombre ?: "Seleccionar...",
                    isReadOnly = isReadOnly, error = error,
                    onClickAction = { if (!isReadOnly) showDialog = true }
                )
                PagedSelectionDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    title = "Seleccionar Agrupación",
                    items = pagedItems,
                    itemContent = { item -> Text(item.nombre ?: "") },
                    onSearch = { agrupacionViewModel.search(it) },
                    onSelect = { onUpdate(entity.copy(agrupacion = it)); showDialog = false }
                )
            }
        ),
    )
    // El resto de la función se mantiene igual, ya que generateAutomaticFieldDescriptors
    // sigue siendo válido para los campos que no son de tipo dropdown.
    return generateAutomaticFieldDescriptors(
        entityClass = Producto::class,
        attributesToIncludeAndOrder = listOf(
            "tipo", "codigo", "descripcion", "activo", "favorito", "familia", "tasaIva", "agrupacion", "unidad", "precio1", "precio2", "precio3",
            "codigoBarra", "codigoBarra2", "precioCosto", "margenGanancia", "productoBalanza", "stock", "stockMinimo", "stockPedido", "proveedor",
            "materiaPrima"
        ),
        customizations = customizations
    )
}