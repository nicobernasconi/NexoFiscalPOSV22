package ar.com.nexofiscal.nexofiscalposv2.screens

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.managers.ClienteManager
import ar.com.nexofiscal.nexofiscalposv2.managers.ProductoManager
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.ui.MenuDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.menuItems
import ar.com.nexofiscal.nexofiscalposv2.utils.TicketPrinter

// Colores y estilos
private val AzulNexo = Color(0xFF00AEEF)
private val GrisClaro = Color(0xFFD6D6D6)
private val Blanco = Color(0xFFFFFFFF)
private val BordeSuave = RoundedCornerShape(5.dp)

/** Tipos de diálogo de listado que podemos mostrar */
private sealed class ListDialog {
    object ListarClientes : ListDialog()
    object ListarProductos : ListDialog()

}

private fun RowScope.ActionIcon(i: Int, modifier: Modifier) {}
class SaleItem(val producto: Producto) {
    var cantidad by mutableStateOf(1)
    var precio by mutableStateOf(producto.precio1)
}

@Composable
fun MainScreen(  onTotalUpdated: (Double) -> Unit ) {
    // Estado de menú y diálogo de listado activo
    var showMenu by remember { mutableStateOf(false) }
    var activeListDialog by remember { mutableStateOf<ListDialog?>(null) }

    // Preferencias y cabeceras
    val context = LocalContext.current
    val prefs: SharedPreferences = context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
    val userName = prefs.getString("nombre_completo", "") ?: ""
    // Estados para clientes
    val clients = remember { mutableStateListOf<Cliente>() }
    var loadingClients by remember { mutableStateOf(false) }

    // 3) Estados para productos
    val products = remember { mutableStateListOf<Producto>() }
    var loadingProducts by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Producto?>(null) }

    val saleItems = remember { mutableStateListOf<SaleItem>() }


    // Aquí: productos que vendemos
    val saleProducts = remember { mutableStateListOf<Producto>() }
    // Total derivado

    val total by remember(saleItems, saleItems.map { Triple(it.cantidad, it.precio, it.producto.id) }) { // Observar cambios en cantidad y precio
        derivedStateOf {
            saleItems.sumOf { it.cantidad * it.precio }
        }
    }

    // NUEVO: Efecto para llamar a onTotalUpdated cuando el total cambie
    LaunchedEffect(total) {
        onTotalUpdated(total)
    }

    // Contexto y prefs
    val activity = context as? Activity
    val token = prefs.getString("token", "") ?: ""
    var headers = mutableMapOf<String, String>().apply {
        put("Authorization", "Bearer $token")
    }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }


    val ticketPrinter = remember { TicketPrinter(context) }

    Box(Modifier.fillMaxSize()) {
        // ─── UI principal (Home) ─────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blanco)
        ) {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulNexo)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.header_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(width = 175.dp, height = 36.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = Blanco)
                }
            }

            // Botones cliente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulNexo)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { activeListDialog = ListDialog.ListarClientes },
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) {
                    Text("CLIENTES", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) {
                    Text("AGREGAR CLIENTE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            @Composable
            fun RowScope.ActionIcon(
                iconResId: Int,
                modifier: Modifier = Modifier, // Este modifier será para el Box que forma el cuadrado
                contentDescription: String? = null, // Para accesibilidad
                onClick: () -> Unit = { /* Acción por defecto vacía */ } // Para la acción
            ) {
                Box(
                    modifier = modifier // Aplicar el modifier aquí (que debería definir el tamaño cuadrado)
                        .background(
                            color = AzulNexo, // Usar el color definido
                            shape = RoundedCornerShape(5.dp) // Esquinas redondeadas de 5dp
                        )
                        .clickable(onClick = onClick), // Para que toda el área sea clickeable
                    contentAlignment = Alignment.Center // Para centrar el Icon dentro del Box
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = contentDescription,
                        tint = Color.White, // El icono será blanco, como en tu IconButton original
                        modifier = Modifier.size(24.dp) // Un tamaño para el icono dentro del Box, ajústalo si es necesario
                    )
                }
            }

            Row(
     modifier = Modifier
         .fillMaxWidth()
         .background(GrisClaro) // El fondo de la fila contenedora
         .padding(8.dp),
     horizontalArrangement = Arrangement.SpaceEvenly,
     verticalAlignment = Alignment.CenterVertically
 ) {
     // Define un tamaño para el fondo cuadrado de cada ActionIcon.
     // Elige el tamaño que desees para el cuadrado. Por ejemplo, 48dp x 48dp.
     val actionButtonSquareSize = Modifier.size(48.dp)

     ActionIcon(
         iconResId = R.drawable.ic_search,
         modifier = actionButtonSquareSize, // Pasa el tamaño del cuadrado
         contentDescription = "Buscar",
         onClick = {  activeListDialog = ListDialog.ListarProductos }
     )
     ActionIcon(
         iconResId = R.drawable.ic_barcode,
         modifier = actionButtonSquareSize,
         contentDescription = "Código de Barras",
         onClick = { /* TODO: Implementa la acción de código de barras */ }
     )
     ActionIcon(
         iconResId = R.drawable.ic_edit,
         modifier = actionButtonSquareSize,
         contentDescription = "Editar",
         onClick = { /* TODO: Implementa la acción de editar */ }
     )
     ActionIcon(
         iconResId = R.drawable.ic_add,
         modifier = actionButtonSquareSize,
         contentDescription = "Agregar",
         onClick = { /* TODO: Implementa la acción de agregar */ }
     )
 }

            // Cabecera de lista
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Producto", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(
                    "Cantidad",
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Precio",
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Subtotal",
                    Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )

            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                saleItems.forEach { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.producto.descripcion.orEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        // Campo cantidad
                        OutlinedTextField(
                            value = item.cantidad.toString(),
                            onValueChange = { str ->
                                str.toIntOrNull()?.let { item.cantidad = it }
                            },
                            modifier = Modifier
                                .width(80.dp)
                                .padding(horizontal = 4.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Cant") })
                        // Campo precio unitario
                        OutlinedTextField(
                            value = "%.2f".format(item.precio),
                            onValueChange = { str ->
                                str.toDoubleOrNull()?.let { item.precio = it }
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .padding(start = 4.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Precio") })
                        // Subtotal
                        Text(
                            text = "$${"%.2f".format(item.cantidad * item.precio)}",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            textAlign = TextAlign.End
                        )
                        // Botón eliminar
                                                IconButton(
                                                    onClick = { saleItems.remove(item) },
                                                    modifier = Modifier.padding(start = 4.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_delete),
                                                        contentDescription = "Eliminar",
                                                        tint = Color.Red
                                                    )
                                                }
                    }
                }
            }

            // Total recalculado automáticamente
            Text(
                text = "$${"%.2f".format(total)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                textAlign = TextAlign.End
            )
            // Botones finales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        ticketPrinter.printTicket(
                            context, saleItems.map { it.producto }, clienteSeleccionado,


                            1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = GrisClaro)
                ) { Text("COBRAR", color = Color.Black, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = GrisClaro)
                ) { Text("PEDIDO", color = Color.Black, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = GrisClaro)
                ) { Text("PRESUPUESTO", color = Color.Black, fontWeight = FontWeight.Bold) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulNexo)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) { Text("PLU DIRECTOS", color = Color.Black, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) { Text("COMPROBANTES", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        }
        //obtener el valor de la preferencia
        val sharedPreferences: SharedPreferences =
            LocalContext.current.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
        // Diálogo de menú
        var showMenu = false
        MenuDialog(
            isVisible = showMenu,
            userName = sharedPreferences.getString("nombre_completo", "") ?: "",
            items = menuItems,
            onDismiss = { showMenu = false },
            onItemSelected = { selection ->
                // Manejar selección
                println("Seleccionado: $selection")
            })
    }

    // ─── Menú jerárquico ───────────────────────────────────────
    MenuDialog(
        isVisible = showMenu,
        userName = userName,
        items = menuItems,
        onDismiss = { showMenu = false },
        onItemSelected = { title ->
            showMenu = false
            activeListDialog = when (title) {
                "Listar Clientes" -> ListDialog.ListarClientes
                "Listar Productos" -> ListDialog.ListarProductos
                else -> null
            }
        })

    // ─── Diálogo de listado ───────────────────────────────────
    activeListDialog?.let { dialogType ->
        Dialog(onDismissRequest = { activeListDialog = null }) {
            Surface(
                tonalElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (dialogType) {
                    // ─── LISTAR CLIENTES ─────────────────────────────────────────
                    ListDialog.ListarClientes -> {
                        // Lanzamos carga de clientes solo al abrir
                        LaunchedEffect(dialogType) {
                            if (dialogType == ListDialog.ListarClientes) {
                                loadingClients = true
                                clients.clear()
                                ClienteManager.obtenerClientes(
                                    headers as MutableMap<String?, String?>?,
                                    object : ClienteManager.ClienteListCallback {
                                        override fun onSuccess(list: MutableList<Cliente?>?) {
                                            activity?.runOnUiThread {
                                                list?.filterNotNull()?.let { clients.addAll(it) }
                                                loadingClients = false
                                            }
                                        }

                                        override fun onError(errorMessage: String?) {
                                            activity?.runOnUiThread {
                                                loadingClients = false
                                                NotificationManager.show(
                                                    message = errorMessage
                                                        ?: "Error cargando clientes",
                                                    type = NotificationType.ERROR
                                                )
                                            }
                                        }
                                    })
                            }
                        }

                        if (loadingClients) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            CrudListScreen(
                                title = "Clientes",
                                items = clients,
                                itemLabel = { "${it.nombre} (${it.cuit})" },
                                onEdit = { /*…*/ },
                                onSelect = { cliente ->
                                    clienteSeleccionado = cliente
                                    activeListDialog = null
                                    NotificationManager.show(
                                        "Seleccionado ${cliente.nombre}", NotificationType.SUCCESS
                                    )
                                },
                                onDelete = { /*…*/ },
                                type = "R"
                            )
                        }
                    }

                    // ─── LISTAR PRODUCTOS ─────────────────────────────────────────
                    ListDialog.ListarProductos -> {
                        // Lanzamos carga de productos solo al abrir
                        LaunchedEffect(dialogType) {
                            if (dialogType == ListDialog.ListarProductos) {
                                loadingProducts = true
                                products.clear()
                                ProductoManager.obtenerProductos(
                                    headers as MutableMap<String?, String?>?,
                                    "",
                                    object : ProductoManager.ProductoListCallback {
                                        override fun onSuccess(list: MutableList<Producto?>?) {
                                            activity?.runOnUiThread {
                                                list?.filterNotNull()?.let { products.addAll(it) }
                                                loadingProducts = false
                                            }
                                        }

                                        override fun onError(errorMessage: String?) {
                                            activity?.runOnUiThread {
                                                loadingProducts = false
                                                NotificationManager.show(
                                                    message = errorMessage
                                                        ?: "Error cargando productos",
                                                    type = NotificationType.ERROR
                                                )
                                            }
                                        }
                                    })
                            }
                        }

                        if (loadingProducts) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            CrudListScreen(
                                title = "Productos",
                                items = products,
                                itemLabel = { "${it.descripcion} - $${it.precio1}" },
                                onEdit = {},
                                onSelect = { prod ->
                                    saleItems.add(SaleItem(prod))
                                    // 2) Cierra diálogo
                                    activeListDialog = null
                                    // 3) Mensaje
                                    NotificationManager.show(
                                        "Agregaste ${prod.descripcion}", NotificationType.SUCCESS
                                    )
                                },
                                onDelete = { prod ->
                                    selectedProduct = prod
                                    activeListDialog = null
                                    NotificationManager.show(
                                        "Eliminar ${prod.descripcion} no implementado",
                                        NotificationType.ERROR
                                    )
                                },
                                type = "R"
                            )
                        }
                    }
                }
            }
        }
    }

    // ─── Loading y notificaciones ─────────────────────────────
    LoadingDialog(show = false)
    NotificationHost()
}


@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(32.dp), contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 18.sp, color = Color.Gray)
    }
}