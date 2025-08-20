package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector

// Modelo de ítem de menú con posibles submenús (ahora usa ImageVector)
data class MenuItemModel(
    val icon: ImageVector,
    val title: String,
    val subItems: List<MenuItemModel> = emptyList()
)

// Lista de categorías y subcategorías con íconos de Material Icons
val menuItems = listOf(
    MenuItemModel(Icons.Outlined.Inventory, "Productos", listOf(
        MenuItemModel(Icons.Outlined.Add, "Crear Producto"),
        MenuItemModel(Icons.Outlined.List, "Listar Productos"),
        MenuItemModel(Icons.Outlined.Inventory, "Stock de Productos"),
        MenuItemModel(Icons.Outlined.Edit, "Ajuste de Stock"),
    )),
    MenuItemModel(Icons.Outlined.People, "Clientes", listOf(
        MenuItemModel(Icons.Outlined.Add, "Crear Cliente"),
        MenuItemModel(Icons.Outlined.List, "Listar Clientes"),
    )),
    MenuItemModel(Icons.Outlined.ReceiptLong, "Ventas", listOf(
        MenuItemModel(Icons.Outlined.List, "Listar Ventas"),
        MenuItemModel(Icons.Outlined.BarChart, "Informe de Ventas"),
    )),
    MenuItemModel(Icons.Outlined.AccountBalanceWallet, "Caja", listOf(
        MenuItemModel(Icons.Outlined.ReceiptLong, "Cierres de Caja"),
        MenuItemModel(Icons.Outlined.List, "Listar Cierres de Caja"),
        MenuItemModel(Icons.Outlined.ReceiptLong, "Listar Gastos"),
        MenuItemModel(Icons.Outlined.Add, "Agregar Gasto"),
    )),
    MenuItemModel(Icons.Outlined.LocalShipping, "Proveedores", listOf(
        MenuItemModel(Icons.Outlined.Add, "Crear Proveedor"),
        MenuItemModel(Icons.Outlined.List, "Listar Proveedores"),
    )),
    MenuItemModel(Icons.Outlined.Category, "Promociones", listOf(
        MenuItemModel(Icons.Outlined.Add, "Crear Promoción"),
        MenuItemModel(Icons.Outlined.List, "Listar Promociones"),
    )),
    MenuItemModel(Icons.Outlined.Settings, "Configuración", listOf(
        MenuItemModel(Icons.Outlined.SystemUpdateAlt, "Descargar Datos"),
        MenuItemModel(Icons.Outlined.UploadFile, "Subir Cambios"),
        MenuItemModel(Icons.Outlined.Category, "Agrupaciones"),
        MenuItemModel(Icons.Outlined.Category, "Categorias"),
        MenuItemModel(Icons.Outlined.Group, "Familias"),
        MenuItemModel(Icons.Outlined.CreditCard, "Formas de Pago"),
        MenuItemModel(Icons.Outlined.Description, "Tipos de Documento"),
        MenuItemModel(Icons.Outlined.Payments, "Tipos de IVA"),
        MenuItemModel(Icons.Outlined.Straighten, "Unidades"),
        MenuItemModel(Icons.Outlined.Lock, "Modo Kiosco"),
        MenuItemModel(Icons.Outlined.Settings, "Configuración General")
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
                        }, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    IconButton(onClick = onDismiss, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.title, fontSize = 16.sp)
                            if (item.subItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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