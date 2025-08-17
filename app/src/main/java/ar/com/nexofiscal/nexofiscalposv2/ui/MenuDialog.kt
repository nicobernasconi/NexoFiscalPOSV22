package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ar.com.nexofiscal.nexofiscalposv2.R

// Modelo de ítem de menú con posibles submenús
data class MenuItemModel(
    val iconRes: Int,
    val title: String,
    val subItems: List<MenuItemModel> = emptyList()
)

// Lista de categorías y subcategorías
val menuItems = listOf(
    MenuItemModel(R.drawable.ic_inventory, "Productos", listOf(
        MenuItemModel(R.drawable.ic_add, "Crear Producto"),
        MenuItemModel(R.drawable.ic_list, "Listar Productos"),
        MenuItemModel(R.drawable.ic_inventory, "Stock de Productos"),
    )),
    MenuItemModel(R.drawable.ic_people, "Clientes", listOf(
        MenuItemModel(R.drawable.ic_add, "Crear Cliente"),
        MenuItemModel(R.drawable.ic_list, "Listar Clientes"),
    )),
    MenuItemModel(R.drawable.ic_pos, "Ventas", listOf(
        MenuItemModel(R.drawable.ic_list, "Listar Ventas"),
        MenuItemModel(R.drawable.ic_list, "Informe de Ventas"),
        MenuItemModel(R.drawable.ic_list, "Cierres de Caja"),

    )),
    MenuItemModel(R.drawable.ic_shipping, "Proveedores", listOf(
        MenuItemModel(R.drawable.ic_add, "Crear Proveedor"),
        MenuItemModel(R.drawable.ic_list, "Listar Proveedores"),
    )),
    MenuItemModel(R.drawable.ic_giftcard, "Promociones", listOf(
        MenuItemModel(R.drawable.ic_add, "Crear Promoción"),
        MenuItemModel(R.drawable.ic_list, "Listar Promociones"),
    )),
    MenuItemModel(R.drawable.ic_settings, "Configuración", listOf(
        MenuItemModel(R.drawable.ic_download, "Descargar Datos"),
        MenuItemModel(R.drawable.ic_upload, "Subir Cambios"),
        MenuItemModel(R.drawable.ic_settings, "Agrupaciones"),
        MenuItemModel(R.drawable.ic_category, "Categorias"),
        MenuItemModel(R.drawable.ic_family, "Familias"),
        MenuItemModel(R.drawable.ic_payment, "Formas de Pago"),
        MenuItemModel(R.drawable.ic_document, "Tipos de Documento"),
        MenuItemModel(R.drawable.ic_tax, "Tipos de IVA"),
        MenuItemModel(R.drawable.ic_units, "Unidades"),
        MenuItemModel(R.drawable.ic_lock, "Modo Kiosco"),
        MenuItemModel(R.drawable.ic_settings, "Configuración General")

    ))
)

/**
 * Diálogo de menú en Compose con navegación jerárquica
 */
@Composable
fun MenuDialog(
    isVisible: Boolean,
    userName: String,
    items: List<MenuItemModel>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    if (!isVisible) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            var currentItems by remember { mutableStateOf(items) }
            var history by remember { mutableStateOf<List<Pair<String, List<MenuItemModel>>>>(emptyList()) }
            Column(modifier = Modifier.padding(16.dp)) {
                // Header con nombre y botones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = {
                            val last = history.last()
                            history = history.dropLast(1)
                            currentItems = last.second
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Atrás"
                            )
                        }
                    }
                    Text(
                        text = history.lastOrNull()?.first ?: userName,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                // Lista de opciones
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    currentItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.subItems.isNotEmpty()) {
                                        history = history + (item.title to currentItems)
                                        currentItems = item.subItems
                                    } else {
                                        onItemSelected(item.title)
                                    }
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.title, fontSize = 16.sp)
                            if (item.subItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = "Siguiente"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}