package ar.com.nexofiscal.nexofiscalposv2.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Base64
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.SyncService
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.AgrupacionViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CategoriaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CierreCajaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ClienteViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ComprobanteViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ConfiguracionViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.FamiliaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.FormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.LocalidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.MonedaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PaisViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProductoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.PromocionViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProveedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProvinciaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.RenglonComprobanteViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.RolViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.SucursalViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TasaIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoComprobanteViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoDocumentoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoFormaPagoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoIvaViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.TipoViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.UnidadViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.UsuarioViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.VendedorViewModel
import ar.com.nexofiscal.nexofiscalposv2.managers.LogoutManager
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.managers.SyncManager
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPagoComprobante
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.screens.config.AgrupacionScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getMainClientFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getMainProductFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getPromocionFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.config.getProveedorFieldDescriptors
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.EntityEditScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingManager
import ar.com.nexofiscal.nexofiscalposv2.ui.LogoutConfirmationDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.MenuDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.PrintingStatusDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.PrintingUiManager
import ar.com.nexofiscal.nexofiscalposv2.ui.menuItems
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.CC
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.GrisClaro
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.RojoError
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.TextoGrisOscuro
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleItem(val producto: Producto) {
    var cantidad by mutableStateOf(1.0)
        private set
    var precio by mutableStateOf(producto.precio1)
        private set

    fun updateCantidad(newCantidad: Double) {
        if (newCantidad >= 0) cantidad = newCantidad
    }

    fun updatePrecio(newPrecio: Double) {
        if (newPrecio >= 0) precio = newPrecio
    }

    val subtotal: Double get() = cantidad * precio
}

@Composable
fun MainScreen(
    onTotalUpdated: (Double) -> Unit,
    productoViewModel: ProductoViewModel,
    clienteViewModel: ClienteViewModel,
    formaPagoViewModel: FormaPagoViewModel,
    tipoViewModel: TipoViewModel,
    familiaViewModel: FamiliaViewModel,
    tasaIvaViewModel: TasaIvaViewModel,
    unidadViewModel: UnidadViewModel,
    proveedorViewModel: ProveedorViewModel,
    agrupacionViewModel: AgrupacionViewModel,
    comprobanteViewModel: ComprobanteViewModel,
    renglonComprobanteViewModel: RenglonComprobanteViewModel,
    tipoDocumentoViewModel: TipoDocumentoViewModel,
    tipoIvaViewModel: TipoIvaViewModel,
    categoriaViewModel: CategoriaViewModel,
    tipoFormaPagoViewModel: TipoFormaPagoViewModel,
    localidadViewModel: LocalidadViewModel,
    promocionViewModel: PromocionViewModel,
    paisViewModel: PaisViewModel,
    provinciaViewModel: ProvinciaViewModel,
    rolViewModel: RolViewModel,
    sucursalViewModel: SucursalViewModel,
    configuracionViewModel: ConfiguracionViewModel,
    usuarioViewModel: UsuarioViewModel,
    vendedorViewModel: VendedorViewModel,
    cierreCajaViewModel: CierreCajaViewModel,
    tipoComprobanteViewModel: TipoComprobanteViewModel,
    monedaViewModel: MonedaViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAgrupacionesScreen by remember { mutableStateOf(false) }
    var showCategoriaScreen by remember { mutableStateOf(false) }
    var showCierreCajaScreen by remember { mutableStateOf(false) }
    var showClienteListDialog by remember { mutableStateOf(false) }
    var showClienteEditScreen by remember { mutableStateOf(false) }
    var showComprobantesScreen by remember { mutableStateOf(false) }
    var showFamiliaScreen by remember { mutableStateOf(false) }
    var showFormaPagoScreen by remember { mutableStateOf(false) }
    var showLocalidadScreen by remember { mutableStateOf(false) }
    var showPaisScreen by remember { mutableStateOf(false) }
    var showPromocionScreen by remember { mutableStateOf(false) }
    var showProveedorScreen by remember { mutableStateOf(false) }
    var showProvinciaScreen by remember { mutableStateOf(false) }
    var showRolScreen by remember { mutableStateOf(false) }
    var showSucursalScreen by remember { mutableStateOf(false) }
    var showTasaIvaScreen by remember { mutableStateOf(false) }
    var showTipoComprobanteScreen by remember { mutableStateOf(false) }
    var showTipoDocumentoScreen by remember { mutableStateOf(false) }
    var showTipoFormaPagoScreen by remember { mutableStateOf(false) }
    var showTipoIvaScreen by remember { mutableStateOf(false) }
    var showTipoScreen by remember { mutableStateOf(false) }
    var showUnidadScreen by remember { mutableStateOf(false) }
    var showUsuarioScreen by remember { mutableStateOf(false) }
    var showVendedorScreen by remember { mutableStateOf(false) }
    var showFullScreenProductSearch by remember { mutableStateOf(false) }
    var showProductEditScreen by remember { mutableStateOf(false) }
    var isProductCreateMode by remember { mutableStateOf(false) }
    var isClientCreateMode by remember { mutableStateOf(false) }
    var productScreenMode by remember { mutableStateOf(CrudScreenMode.EDIT_DELETE) }
    var clientListMode by remember { mutableStateOf(CrudScreenMode.VIEW_SELECT) }
    var showPluDirectos by remember { mutableStateOf(false) }
    var showCobrarScreen by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showKioskConfigScreen by remember { mutableStateOf(false) }
    var showConfiguracionScreen by remember { mutableStateOf(false) }
    var tipoComprobanteActual by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val saleItems = remember { mutableStateListOf<SaleItem>() }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    val total by remember { derivedStateOf { saleItems.sumOf { it.subtotal } } }
    val userName = SessionManager.nombreCompleto ?: ""
    val productDescriptors = remember { getMainProductFieldDescriptors(tipoViewModel, familiaViewModel, tasaIvaViewModel, unidadViewModel, proveedorViewModel, agrupacionViewModel,monedaViewModel) }
    val clientDescriptors = remember { getMainClientFieldDescriptors(tipoDocumentoViewModel, tipoIvaViewModel, localidadViewModel, provinciaViewModel, categoriaViewModel, vendedorViewModel) }
    val scope = rememberCoroutineScope()
    var showSyncStatus by remember { mutableStateOf(false) }
    var showInformeDeVentas by remember { mutableStateOf(false) }
    var showPromocionEditScreen by remember { mutableStateOf(false) }
    var isPromocionCreateMode by remember { mutableStateOf(false) }
    var promocionInScreen by remember { mutableStateOf<Promocion?>(null) }
    var showProveedorEditScreen by remember { mutableStateOf(false) }
    var isProveedorCreateMode by remember { mutableStateOf(false) }
    var proveedorInScreen by remember { mutableStateOf<Proveedor?>(null) }
    LaunchedEffect(total) { onTotalUpdated(total) }

    // --- Lógica de Edición y Carga ---
    val clientInScreen by clienteViewModel.clienteParaEditar.collectAsState()
    val productInScreen by productoViewModel.productoParaEditar.collectAsState()

    LaunchedEffect(clientInScreen) {
        if (clientInScreen != null && !isClientCreateMode) {
            showClienteListDialog = false
            showClienteEditScreen = true
        }
    }

    LaunchedEffect(productInScreen) {
        if (productInScreen != null && !isProductCreateMode) {
            showFullScreenProductSearch = false
            showProductEditScreen = true
        }
    }


    suspend fun finalizarVenta(tipoComprobanteId: Int, resultado: ResultadoCobro, imprimir: Boolean) {
        if (saleItems.isEmpty()) {
            NotificationManager.show("No hay productos en la venta.", NotificationType.WARNING)
            return
        }

        if (tipoComprobanteId == 1 && resultado.pagos.isEmpty() && total > 0) {
            NotificationManager.show("Debe agregar al menos un pago.", NotificationType.SUCCESS)
            return
        }
        val formasDePagoComprobante = resultado.pagos.map { pago ->
            FormaPagoComprobante(
                id = pago.formaPago.id,
                nombre = pago.formaPago.nombre ?: "",
                porcentaje = pago.formaPago.porcentaje,
                importe = String.format(Locale.US, "%.2f", pago.monto),
                tipoFormaPago = pago.formaPago.tipoFormaPago ?: TipoFormaPago()
            )
        }

        var numeroDeComprobante: Int? = null
        var numeroDeFactura: Int? = null
        if (tipoComprobanteId == 2 || tipoComprobanteId == 3) { // Si es un Pedido o Presupuesto
            numeroDeComprobante = comprobanteViewModel.getNextNumeroForTipo(tipoComprobanteId)
        }
        if (tipoComprobanteId == 1) { // Si es una Venta
            numeroDeComprobante = null // El número lo asigna el servidor o AFIP
        }
        val ahora = Date()
        val fechaStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ahora)
        val horaStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ahora)

        val renglonesDeVenta = saleItems.map {
            RenglonComprobante(
                id = 0, comprobanteId = 0, productoId = it.producto.id,
                descripcion = it.producto.descripcion ?: "", cantidad = it.cantidad,
                precioUnitario = it.precio, tasaIva = it.producto.tasaIva?.tasa ?: 0.0,
                descuento = 0.0, totalLinea = it.subtotal.toString(), producto = it.producto
            )
        }

        val totalOriginal = saleItems.sumOf { it.subtotal }
        val montoDescuento = totalOriginal * (resultado.promociones.sumOf { it.porcentaje } / 100.0)
        val totalFinal = totalOriginal - montoDescuento
        val tipoComprobanteDomain = tipoComprobanteViewModel.getById(tipoComprobanteId)

        // CORRECCIÓN: Se asignan las promociones y formas de pago directamente.
        var comprobanteParaGestionar = Comprobante(
            id = 0, serverId = null, cliente = clienteSeleccionado, clienteId = clienteSeleccionado?.id ?: 1,
            tipoComprobante = tipoComprobanteDomain,
            tipoComprobanteId = tipoComprobanteDomain?.id,
            fecha = fechaStr, hora = horaStr,
            total = totalFinal.toString(), totalPagado = resultado.pagos.sumOf { it.monto },
            descuentoTotal = montoDescuento.toString(), // Guardamos el monto del descuento
            importeIva = renglonesDeVenta.sumOf { (it.totalLinea.toDoubleOrNull() ?: 0.0) / (1 + it.tasaIva) * it.tasaIva },
            numeroFactura = numeroDeFactura,
            puntoVenta = SessionManager.puntoVentaNumero,
            empresaId = SessionManager.empresaId,
            sucursalId = SessionManager.sucursalId,
            numero = numeroDeComprobante,
            vendedorId =null, // Asignamos el vendedor de la sesión
            // --- ASIGNACIÓN CORREGIDA ---
            promociones = resultado.promociones,
            formas_de_pago = formasDePagoComprobante,
            // --- RESTO DE CAMPOS ---
            cuotas = null, remito = null, persona = null, provinciaId = null, fechaBaja = null, motivoBaja = null,
            fechaProceso = null, letra = null, prefijoFactura = null, operacionNegocioId = null, retencionIva = null,
            retencionIibb = null, retencionGanancias = null, porcentajeGanancias = null, porcentajeIibb = null, porcentajeIva = null,
            noGravado = null, condicionVentaId = null, descripcionFlete = null, recibo = null,
            observaciones1 = null, observaciones2 = null, observaciones3 = null, observaciones4 = null, descuento = null,
            descuento1 = null, descuento2 = null, descuento3 = null, descuento4 = null, iva2 = null, impresa = false, cancelado = false,
            nombreCliente = null, direccionCliente = null, localidadCliente = null, garantia = null, concepto = null, notas = null,
            lineaPagoUltima = null, relacionTk = null, totalIibb = null, importeIibb = null, provinciaCategoriaIibbId = null,
            importeRetenciones = null, provinciaIvaProveedorId = null, gananciasProveedorId = null, importeGanancias = null,
            numeroIibb = null, numeroGanancias = null, gananciasProveedor = null, cae = null, fechaVencimiento = null,
            remitoCliente = null, textoDolares = null, comprobanteFinal = null, numeroGuia1 = null, numeroGuia2 = null,
            numeroGuia3 = null, tipoAlicuota1 = null, tipoAlicuota2 = null, tipoAlicuota3 = null, importeIva105 = null,
            importeIva21 = null, importeIva0 = null, noGravadoIva105 = null, noGravadoIva21 = null, noGravadoIva0 = null,
            direccionEntrega = null, fechaEntrega = null, horaEntrega = null, tipoFactura = null, tipoDocumento = null,
            qr = null, comprobanteIdBaja = null, incrementoTotal = null, numeroDeDocumento = null,
            vendedor = null, provincia = null, localId = 0
        )


        if (tipoComprobanteId == 1) {
            try {
                val afipJson = JSONObject().apply {
                    put("ver", 1)
                    put("fecha", fechaStr)
                    put("cuit", SessionManager.empresaCuit?.replace("-", "")?.toLongOrNull() ?: 0)
                    put("ptoVta", SessionManager.puntoVentaNumero)
                    put("tipoCmp", tipoComprobanteDomain?.numero ?: 1)
                    put("nroCmp", comprobanteParaGestionar.numeroFactura ?: 0)
                    // CORRECCIÓN: Manejo de total nulo
                    put("importe", comprobanteParaGestionar.total?.toDoubleOrNull() ?: 0.0)
                    put("moneda", "PES")
                    put("ctz", 1)
                    put("tipoDocRec", clienteSeleccionado?.tipoDocumento?.id ?: 99)
                    put("nroDocRec", clienteSeleccionado?.numeroDocumento?.toLongOrNull() ?: 0)
                    put("tipoCodAut", "E")
                    put("codAut", comprobanteParaGestionar.cae?.toLongOrNull() ?: 0)
                }
                val jsonString = afipJson.toString()
                val base64String = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP)
                val qrUrl = "https://www.afip.gob.ar/fe/qr/?p=$base64String"

                comprobanteParaGestionar = comprobanteParaGestionar.copy(qr = qrUrl)
                Log.d("FinalizarVenta", "QR Data Generado: $qrUrl")

            } catch (e: Exception) {
                Log.e("FinalizarVenta", "Error al generar el JSON para el QR", e)
                NotificationManager.show("Error generando QR", NotificationType.ERROR)
            }
        }


        if (imprimir) {
            try {
                // Inicia el diálogo de "Imprimiendo..."
                PrintingUiManager.startPrinting()
                // Intenta imprimir
                PrintingManager.print(context, comprobanteParaGestionar, renglonesDeVenta)
                // Si no hay errores, cierra el diálogo
                PrintingUiManager.finishPrinting()
            } catch (e: Exception) {
                // Si hay un error de impresión, muestra el diálogo de error.
                PrintingUiManager.showError(e.message ?: "Ocurrió un error desconocido.")
            }
        }

        scope.launch(Dispatchers.IO) {
            val nuevoId = comprobanteViewModel.saveVentaCompleta(
                comprobante = comprobanteParaGestionar,
                renglones = renglonesDeVenta,
                pagos = resultado.pagos,
                promociones = resultado.promociones
            )

            if (nuevoId > 0) {
                launch(Dispatchers.Main) {
                    saleItems.clear()
                    clienteSeleccionado = null
                    val tipoTexto = when (tipoComprobanteId) { 1 -> "Venta"; 2 -> "Presupuesto"; 3 -> "Pedido"; else -> "Comprobante"}
                    NotificationManager.show("$tipoTexto generado con éxito.", NotificationType.SUCCESS)
                }
            } else {
                launch(Dispatchers.Main) {
                    NotificationManager.show("Error al guardar el comprobante.", NotificationType.ERROR)
                }
            }
        }
    }


    var syncState by remember { mutableStateOf(SyncService.SyncState.IDLE) }


    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val stateName = intent?.getStringExtra(SyncService.EXTRA_SYNC_STATE)
                syncState = stateName?.let { SyncService.SyncState.valueOf(it) } ?: SyncService.SyncState.IDLE
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, IntentFilter(SyncService.SYNC_STATE_ACTION))
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(Blanco)) {
            HeaderSection { showMenu = true }
            NotificationHost()
            PrintingStatusDialog()

            ClientInfoSection(
                cliente = clienteSeleccionado,
                onListClients = { clientListMode = CrudScreenMode.VIEW_SELECT; showClienteListDialog = true },
                onAddClient = {
                    isClientCreateMode = true
                    clienteViewModel.limpiarClienteParaEdicion()
                    showClienteEditScreen = true
                },
                onClearClient = { clienteSeleccionado = null }
            )
            ActionButtonsSection(
                onSearchProducts = { productScreenMode = CrudScreenMode.VIEW_SELECT; showFullScreenProductSearch = true },
                onScanBarcode = { showBarcodeScanner = true },
                onEdit = { productScreenMode = CrudScreenMode.EDIT_DELETE; showFullScreenProductSearch = true },
                onAdd = {
                    isProductCreateMode = true
                    productoViewModel.limpiarProductoParaEdicion()
                    showProductEditScreen = true
                }
            )
            SaleItemsList(saleItems, { saleItems.remove(it) }, Modifier.weight(1f))
            TotalSection(total)

            ActionButtonsBottom(
                onCobrar = {
                    if (saleItems.isNotEmpty()) {
                        tipoComprobanteActual = 1
                        showCobrarScreen = true
                    } else {
                        NotificationManager.show("Agregue productos para poder cobrar.", NotificationType.INFO)
                    }
                },
                onPedido = {
                    if (saleItems.isNotEmpty()) {
                        tipoComprobanteActual = 3
                        showCobrarScreen = true
                    } else {
                        NotificationManager.show("Agregue productos para crear un pedido.", NotificationType.INFO)
                    }
                },
                onPresupuesto = {
                    if (saleItems.isNotEmpty()) {
                        tipoComprobanteActual = 2
                        showCobrarScreen = true
                    } else {
                        NotificationManager.show("Agregue productos para crear un presupuesto.", NotificationType.INFO)
                    }
                }
            )
            BottomButtonsSection({ showPluDirectos = true }, { showComprobantesScreen = true })
        }

        MenuDialog(showMenu, userName, menuItems, { showMenu = false }) { title ->
            showMenu = false
            when (title) {
                "Salir" -> showLogoutDialog = true
                "Listar Clientes" -> { clientListMode = CrudScreenMode.EDIT_DELETE; showClienteListDialog = true }
                "Crear Cliente" -> { isClientCreateMode = true; clienteViewModel.limpiarClienteParaEdicion(); showClienteEditScreen = true }
                "Listar Productos" -> { productScreenMode = CrudScreenMode.EDIT_DELETE; showFullScreenProductSearch = true }
                "Crear Producto" -> { isProductCreateMode = true; productoViewModel.limpiarProductoParaEdicion(); showProductEditScreen = true }
                "Informe de Ventas" -> {showInformeDeVentas = true}
                "Descargar Datos" -> {
                    scope.launch {
                        val token = SessionManager.token
                        if (token.isNullOrBlank()) {
                            NotificationManager.show("Error: No se puede sincronizar. Inicie sesión nuevamente.", NotificationType.ERROR)
                            return@launch
                        }
                        NotificationManager.show("Iniciando descarga de datos...", NotificationType.INFO)
                        LoadingManager.show()
                        try {
                            SyncManager.startFullSync(context, token)
                            NotificationManager.show("Sincronización de datos completada.", NotificationType.SUCCESS)
                        } catch (e: Exception) {
                            Log.e("SyncTrigger", "Error durante la sincronización manual", e)
                            NotificationManager.show("Ocurrió un error durante la sincronización.", NotificationType.ERROR)
                        } finally {
                            LoadingManager.hide()
                        }
                    }
                }
                "Subir Cambios" -> {
                    scope.launch {
                        val token = SessionManager.token
                        if (token.isNullOrBlank()) {
                            NotificationManager.show("Error: No se pudo iniciar la subida. Inicie sesión nuevamente.", NotificationType.ERROR)
                            return@launch
                        }
                        NotificationManager.show("Iniciando subida de cambios locales...", NotificationType.INFO)
                        LoadingManager.show()
                        try {
                            UploadManager.uploadLocalChanges(context, token)
                            NotificationManager.show("La subida de cambios ha finalizado.", NotificationType.SUCCESS)
                        } catch (e: Exception) {
                            Log.e("UploadTrigger", "Error durante la subida manual", e)
                            NotificationManager.show("Ocurrió un error durante la subida.", NotificationType.ERROR)
                        } finally {
                            LoadingManager.hide()
                        }
                    }
                }
                "Agrupaciones" -> showAgrupacionesScreen = true
                "Categorias" -> showCategoriaScreen = true
                "Cierres de Caja" -> showCierreCajaScreen = true
                "Familias" -> showFamiliaScreen = true
                "Formas de Pago" -> showFormaPagoScreen = true
                "Localidades" -> showLocalidadScreen = true
                "Países" -> showPaisScreen = true
                "Listar Promociones" -> showPromocionScreen = true
                "Crear Promoción" -> {
                    isPromocionCreateMode = true
                    promocionInScreen = Promocion() // Creamos una instancia vacía para el formulario
                    showPromocionEditScreen = true
                }
                "Listar Proveedores" -> showProveedorScreen = true
                "Crear Proveedor" -> {
                    isProveedorCreateMode = true
                    proveedorInScreen = Proveedor() // Creamos una instancia vacía para el formulario
                    showProveedorEditScreen = true
                }
                "Provincias" -> showProvinciaScreen = true
                "Roles" -> showRolScreen = true
                "Sucursales" -> showSucursalScreen = true
                "Tasas de IVA" -> showTasaIvaScreen = true
                "Tipos de Comprobante" -> showTipoComprobanteScreen = true
                "Tipos de Documento" -> showTipoDocumentoScreen = true
                "Tipos de Forma de Pago" -> showTipoFormaPagoScreen = true
                "Tipos de IVA" -> showTipoIvaScreen = true
                "Tipos" -> showTipoScreen = true
                "Unidades" -> showUnidadScreen = true
                "Usuarios" -> showUsuarioScreen = true
                "Modo Kiosco" -> showKioskConfigScreen = true
                "Configuración General" -> showConfiguracionScreen = true
            }
        }

        if (showClienteListDialog) {
            ClientListDialog(clienteViewModel, clientListMode,
                onClientSelected = { cliente -> clienteSeleccionado = cliente; showClienteListDialog = false; NotificationManager.show("Seleccionado ${cliente.nombre}", NotificationType.SUCCESS) },
                onDismiss = { showClienteListDialog = false },
                onAttemptEdit = { cliente ->
                    isClientCreateMode = false
                    clienteViewModel.cargarClienteParaEdicion(cliente.localId)
                },

                 onDelete  = { cliente ->
                    // Convertimos el modelo 'Cliente' a 'ClienteEntity' antes de borrar.
                    clienteViewModel.delete(cliente.toEntity())
                }

            )
        }

        if (showClienteEditScreen) {
            val entityToEdit = if (isClientCreateMode) remember { Cliente() } else clientInScreen
            if (entityToEdit != null) {
                val title = if (isClientCreateMode) "Crear Cliente" else "Editar Cliente: ${entityToEdit.nombre}"
                Surface(modifier = Modifier.fillMaxSize()) {
                    EntityEditScreen(title, entityToEdit, clientDescriptors,
                        onSave = { updatedClient ->

                            clienteViewModel.save(updatedClient)
                            showClienteEditScreen = false
                            clienteViewModel.limpiarClienteParaEdicion()
                            isClientCreateMode = false
                            val action = if (isClientCreateMode) "creado" else "actualizado"
                            NotificationManager.show("Cliente '${updatedClient.nombre}' $action.", NotificationType.SUCCESS)
                        },
                        onCancel = {
                            showClienteEditScreen = false
                            clienteViewModel.limpiarClienteParaEdicion()
                            isClientCreateMode = false
                        }
                    )
                }
            }
        }

        if (showFullScreenProductSearch) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ProductListContent(
                    productoViewModel = productoViewModel,
                    onProductSelected = { saleItems.add(SaleItem(it)); showFullScreenProductSearch = false; NotificationManager.show("Agregado ${it.descripcion}", NotificationType.SUCCESS) },
                    screenMode = productScreenMode,
                    onDismiss = { showFullScreenProductSearch = false },
                    onAttemptCreate = {
                        isProductCreateMode = true
                        productoViewModel.limpiarProductoParaEdicion()
                        showProductEditScreen = true
                    },
                    onAttemptEdit = { producto ->
                        isProductCreateMode = false
                        productoViewModel.cargarProductoParaEdicion(producto.localId)
                    },
                     onDelete  = {producto -> productoViewModel.delete(producto)}
                )
            }
        }

        if (showProductEditScreen) {
            val entityToEdit = if (isProductCreateMode) remember { Producto() } else productInScreen
            if (entityToEdit != null) {
                val title = if (isProductCreateMode) "Crear Producto" else "Editar: ${entityToEdit.descripcion ?: ""}"
                Surface(modifier = Modifier.fillMaxSize()) {
                    EntityEditScreen(title, entityToEdit, productDescriptors,
                        onSave = { updatedProduct ->
                            productoViewModel.save(updatedProduct)
                            showProductEditScreen = false
                            productoViewModel.limpiarProductoParaEdicion()
                            isProductCreateMode = false
                            val action = if (isProductCreateMode) "creado" else "actualizado"
                            NotificationManager.show("Producto '${updatedProduct.descripcion}' $action.", NotificationType.SUCCESS)
                        },
                        onCancel = {
                            showProductEditScreen = false
                            productoViewModel.limpiarProductoParaEdicion()
                            isProductCreateMode = false
                        }
                    )
                }
            }
        }

        if (showBarcodeScanner) {
            BarcodeScannerScreen(onDismiss = { showBarcodeScanner = false }) { code ->
                showBarcodeScanner = false
                scope.launch {
                    val foundProduct = productoViewModel.findByBarcode(code)
                    if (foundProduct != null) {
                        saleItems.add(SaleItem(foundProduct))
                        NotificationManager.show("Agregado: ${foundProduct.descripcion}", NotificationType.SUCCESS)
                    } else {
                        NotificationManager.show("Producto con código '$code' no encontrado.", NotificationType.ERROR)
                    }
                }
            }
        }
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    LogoutManager.logout(context)
                }
            )
        }
        if(showPluDirectos) {
            Surface(Modifier.fillMaxSize()) {
                PluDirectosScreen(
                    productoViewModel = productoViewModel,
                    onDismiss = { showPluDirectos = false },
                    onProductSelected = { product ->
                        saleItems.add(SaleItem(product))
                        showPluDirectos = false
                        NotificationManager.show("Agregado: ${product.descripcion}", NotificationType.SUCCESS)
                    }
                )
            }
        }

        if (showAgrupacionesScreen) { Surface(Modifier.fillMaxSize()) { AgrupacionScreen(agrupacionViewModel) { showAgrupacionesScreen = false } } }
        if (showCategoriaScreen) { Surface(Modifier.fillMaxSize()) { CategoriaScreen(categoriaViewModel) { showCategoriaScreen = false } } }
        if (showCierreCajaScreen) { Surface(Modifier.fillMaxSize()) { CierreCajaScreen(cierreCajaViewModel) { showCierreCajaScreen = false } } }

        if (showComprobantesScreen) {
            Dialog({ showComprobantesScreen = false }, DialogProperties(usePlatformDefaultWidth = false)) {
                Surface(Modifier.fillMaxSize()) {
                    ComprobantesScreen(comprobanteViewModel, renglonComprobanteViewModel, clienteViewModel, productoViewModel) {
                        showComprobantesScreen = false
                    }
                }
            }
        }
        if (showCobrarScreen) {
            Surface(modifier = Modifier.fillMaxSize()) {
                CobrarScreen(
                    totalAPagar = total,
                    formaPagoViewModel = formaPagoViewModel,
                    promocionViewModel = promocionViewModel,
                    onDismiss = { showCobrarScreen = false },
                    onGuardar = { resultado ->
                        scope.launch {
                            finalizarVenta(tipoComprobanteActual, resultado, imprimir = false)
                            showCobrarScreen = false
                        }
                    },
                    onImprimir = { resultado ->
                        scope.launch {
                            finalizarVenta(tipoComprobanteActual, resultado, imprimir = true)
                            showCobrarScreen = false
                        }
                    }
                )
            }
        }
        if (showKioskConfigScreen) {
            Surface(Modifier.fillMaxSize()) {
                KioskConfigScreen { showKioskConfigScreen = false }
            }
        }

        if (showInformeDeVentas) {
            Surface(modifier = Modifier.fillMaxSize()) {
                InformeDeVentasScreen(
                    onDismiss = { showInformeDeVentas = false },
                    clienteViewModel = clienteViewModel,
                    tipoComprobanteViewModel = tipoComprobanteViewModel,
                    usuarioViewModel = usuarioViewModel,
                    vendedorViewModel = vendedorViewModel
                )
            }
        }
        if (showConfiguracionScreen) {
            Surface(Modifier.fillMaxSize()) {
                ConfiguracionScreen(viewModel = configuracionViewModel) { showConfiguracionScreen = false }
            }
        }

        if (showPromocionEditScreen && promocionInScreen != null) {
            val fieldDescriptors = remember { getPromocionFieldDescriptors() }
            val title = if (isPromocionCreateMode) "Crear Promoción" else "Editar Promoción: ${promocionInScreen!!.nombre}"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = title,
                    initialEntity = promocionInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedPromocion ->
                        // Guardamos usando el ViewModel correspondiente
                        promocionViewModel.save(updatedPromocion.toEntity())
                        showPromocionEditScreen = false
                        val action = if (isPromocionCreateMode) "creada" else "actualizada"
                        NotificationManager.show("Promoción '${updatedPromocion.nombre}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showPromocionEditScreen = false }
                )
            }
        }
        if (showProveedorEditScreen && proveedorInScreen != null) {
            val fieldDescriptors = remember { getProveedorFieldDescriptors(localidadViewModel, tipoIvaViewModel, categoriaViewModel) }
            val title = if (isProveedorCreateMode) "Crear Proveedor" else "Editar Proveedor: ${proveedorInScreen!!.razonSocial}"

            Surface(modifier = Modifier.fillMaxSize()) {
                EntityEditScreen(
                    title = title,
                    initialEntity = proveedorInScreen!!,
                    fieldDescriptors = fieldDescriptors,
                    onSave = { updatedProveedor ->
                        // Guardamos usando el ViewModel correspondiente
                        proveedorViewModel.save(updatedProveedor.toEntity())
                        showProveedorEditScreen = false
                        val action = if (isProveedorCreateMode) "creado" else "actualizado"
                        NotificationManager.show("Proveedor '${updatedProveedor.razonSocial}' $action.", NotificationType.SUCCESS)
                    },
                    onCancel = { showProveedorEditScreen = false }
                )
            }
        }

        // Si la pantalla de listar proveedores ya existe, esta se encarga de la edición.
        // Si no existe, podemos añadir la lógica de edición aquí también.
        if (showProveedorScreen) {
            ProveedorScreen(
                proveedorViewModel = proveedorViewModel,
                localidadViewModel = localidadViewModel,
                tipoIvaViewModel = tipoIvaViewModel,
                categoriaViewModel = categoriaViewModel,
                onDismiss = { showProveedorScreen = false }
            )
        }
        if (showFamiliaScreen) { Surface(Modifier.fillMaxSize()) { FamiliaScreen(familiaViewModel) { showFamiliaScreen = false } } }
        if (showFormaPagoScreen) { Surface(Modifier.fillMaxSize()) { FormaPagoScreen(formaPagoViewModel, tipoFormaPagoViewModel) { showFormaPagoScreen = false } } }
        if (showLocalidadScreen) { Surface(Modifier.fillMaxSize()) { LocalidadScreen(localidadViewModel, provinciaViewModel) { showLocalidadScreen = false } } }
        if (showPaisScreen) { Surface(Modifier.fillMaxSize()) { PaisScreen(paisViewModel) { showPaisScreen = false } } }
        if (showPromocionScreen) { Surface(Modifier.fillMaxSize()) { PromocionScreen(promocionViewModel) { showPromocionScreen = false } } }
        if (showProveedorScreen) { Surface(Modifier.fillMaxSize()) { ProveedorScreen(proveedorViewModel, localidadViewModel, tipoIvaViewModel, categoriaViewModel) { showProveedorScreen = false } } }
        if (showProvinciaScreen) { Surface(Modifier.fillMaxSize()) { ProvinciaScreen(provinciaViewModel, paisViewModel) { showProvinciaScreen = false } } }
        if (showRolScreen) { Surface(Modifier.fillMaxSize()) { RolScreen(rolViewModel) { showRolScreen = false } } }
        if (showSucursalScreen) { Surface(Modifier.fillMaxSize()) { SucursalScreen(sucursalViewModel) { showSucursalScreen = false } } }
        if (showTasaIvaScreen) { Surface(Modifier.fillMaxSize()) { TasaIvaScreen(tasaIvaViewModel) { showTasaIvaScreen = false } } }
        if (showTipoComprobanteScreen) { Surface(Modifier.fillMaxSize()) { TipoComprobanteScreen(tipoComprobanteViewModel) { showTipoComprobanteScreen = false } } }
        if (showTipoDocumentoScreen) { Surface(Modifier.fillMaxSize()) { TipoDocumentoScreen(tipoDocumentoViewModel) { showTipoDocumentoScreen = false } } }
        if (showTipoFormaPagoScreen) { Surface(Modifier.fillMaxSize()) { TipoFormaPagoScreen(tipoFormaPagoViewModel) { showTipoFormaPagoScreen = false } } }
        if (showTipoIvaScreen) { Surface(Modifier.fillMaxSize()) { TipoIvaScreen(tipoIvaViewModel) { showTipoIvaScreen = false } } }
        if (showTipoScreen) { Surface(Modifier.fillMaxSize()) { TipoScreen(tipoViewModel) { showTipoScreen = false } } }
        if (showUnidadScreen) { Surface(Modifier.fillMaxSize()) { UnidadScreen(unidadViewModel) { showUnidadScreen = false } } }
        if (showUsuarioScreen) { Surface(Modifier.fillMaxSize()) { UsuarioScreen(usuarioViewModel, rolViewModel, sucursalViewModel, vendedorViewModel) { showUsuarioScreen = false } } }
        if (showVendedorScreen) { Surface(Modifier.fillMaxSize()) { VendedorScreen(vendedorViewModel) { showVendedorScreen = false } } }
    }

    SyncStatusIcon(
        state = syncState,
        modifier = Modifier

            .padding(start = 12.dp, top = 12.dp)
    )
}


@Composable
private fun HeaderSection(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CC)
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
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Filled.Menu, "Menú", tint = Blanco)
        }
    }
}



@Composable
private fun ClientInfoSection(
    cliente: Cliente?,
    onListClients: () -> Unit,
    onAddClient: () -> Unit,
    onClearClient: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CC.copy(alpha = 0.9f))
            .padding(8.dp)
    ) {
        if (cliente == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onListClients,
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) {
                    Text("CLIENTES", color = NegroNexo, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onAddClient,
                    modifier = Modifier.weight(1f),
                    shape = BordeSuave,
                    colors = ButtonDefaults.buttonColors(containerColor = Blanco)
                ) {
                    Text("AGREGAR CLIENTE", color = NegroNexo, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().background( NegroNexo).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cliente.nombre ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "CUIT/DNI: ${cliente.cuit ?: "No especificado"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blanco.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onClearClient) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Quitar cliente",
                        tint = Blanco
                    )
                }
            }
        }
    }
}


@Composable
private fun ActionButtonsSection(
    onSearchProducts: () -> Unit,
    onScanBarcode: () -> Unit,
    onEdit: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisClaro)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonSize = Modifier.size(48.dp)
        ActionIcon(R.drawable.ic_search, buttonSize, "Buscar", onSearchProducts)
        ActionIcon(R.drawable.ic_barcode, buttonSize, "Código de Barras", onScanBarcode)
        ActionIcon(R.drawable.ic_edit, buttonSize, "Editar", onEdit)
        ActionIcon(R.drawable.ic_add, buttonSize, "Agregar", onAdd)
    }
}

@Composable
private fun ActionIcon(
    iconResId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(
                color = CC,
                shape = BordeSuave
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Blanco,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SaleItemsList(
    saleItems: List<SaleItem>,
    onRemoveItem: (SaleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Blanco)) {
        if (saleItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Producto", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = TextoGrisOscuro)
                Text("Precio", Modifier.width(90.dp), style = MaterialTheme.typography.labelMedium, color = TextoGrisOscuro, textAlign = TextAlign.Center)
                Text("Subtotal", Modifier.width(100.dp), style = MaterialTheme.typography.labelMedium, color = TextoGrisOscuro, textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(36.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        }

        if (saleItems.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Agregue productos a la venta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
            ) {
                items(saleItems, key = { saleItem -> saleItem.producto.id.toString() + saleItem.hashCode() }) { item ->
                    SaleItemRow(item = item, onRemove = { onRemoveItem(item) })
                }
            }
        }
    }
}

@Composable
private fun SaleItemRow(item: SaleItem, onRemove: () -> Unit) {
    var precioTextFieldValue by remember {
        mutableStateOf(TextFieldValue(String.format(Locale.US, "%.2f", item.precio)))
    }
    var isPriceFocused by remember { mutableStateOf(false) }

    LaunchedEffect(item.precio, isPriceFocused) {
        val modelPriceFormatted = String.format(Locale.US, "%.2f", item.precio)
        if (!isPriceFocused && precioTextFieldValue.text != modelPriceFormatted) {
            precioTextFieldValue = precioTextFieldValue.copy(text = modelPriceFormatted)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = GrisClaro.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = item.producto.descripcion.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Código: ${item.producto.codigo.orEmpty()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(painterResource(R.drawable.ic_delete), "Eliminar", tint = RojoError)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuantitySelector(
                    quantity = item.cantidad,
                    onQuantityChange = { item.updateCantidad(it) }
                )
                Spacer(Modifier.weight(1f))

                BasicTextField(
                    value = precioTextFieldValue,
                    onValueChange = { newTextFieldValue ->
                        if (newTextFieldValue.text.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            precioTextFieldValue = newTextFieldValue
                            item.updatePrecio(newTextFieldValue.text.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    modifier = Modifier
                        .width(90.dp)
                        .onFocusChanged { focusState ->
                            isPriceFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                val end = precioTextFieldValue.text.length
                                precioTextFieldValue = precioTextFieldValue.copy(
                                    selection = TextRange(0, end)
                                )
                            }
                        }
                        .background(
                            color = if (isPriceFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, if (isPriceFocused) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )

                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", item.subtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}


@Composable
private fun QuantitySelector(
    quantity: Double,
    onQuantityChange: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(String.format(Locale.US, "%.3f", quantity)) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(quantity) {
        if (!isFocused) {
            textValue = String.format(Locale.US, "%.3f", quantity)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { if (quantity > 1.0) onQuantityChange(quantity - 1.0) else onQuantityChange(0.0) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(painterResource(R.drawable.ic_minus), "Reducir cantidad", tint = MaterialTheme.colorScheme.primary)
        }

        BasicTextField(
            value = textValue,
            onValueChange = { newText ->
                if (newText.matches(Regex("^\\d*\\.?\\d{0,3}$"))) {
                    textValue = newText
                    val newQuantity = newText.toDoubleOrNull()
                    if (newQuantity != null) {
                        onQuantityChange(newQuantity)
                    }
                }
            },
            modifier = Modifier
                .width(80.dp)
                .onFocusChanged { focusState -> isFocused = focusState.isFocused }
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(vertical = 6.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        IconButton(
            onClick = { onQuantityChange(quantity + 1.0) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(painterResource(R.drawable.ic_add), "Aumentar cantidad", tint = MaterialTheme.colorScheme.primary)
        }
    }
}


@Composable
private fun TotalSection(total: Double) {
    Text(
        text = String.format(Locale.getDefault(), "$%.2f", total),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        textAlign = TextAlign.End,
        color = NegroNexo
    )
}

@Composable
private fun ActionButtonsBottom(onCobrar: () -> Unit, onPedido: () -> Unit, onPresupuesto: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val buttonModifier = Modifier.weight(1f)
        Button(
            onClick = onCobrar,
            modifier = buttonModifier,
            shape = BordeSuave,
            colors = ButtonDefaults.buttonColors(containerColor = CC)
        ) { Text("COBRAR", color = Blanco) }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onPedido,
            modifier = buttonModifier,
            shape = BordeSuave,
            colors = ButtonDefaults.buttonColors(containerColor = CC)
        ) { Text("PEDIDO", color = Blanco) }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onPresupuesto,
            modifier = buttonModifier,
            shape = BordeSuave,
            colors = ButtonDefaults.buttonColors(containerColor = CC),

            ) {  Text("PRESUPUESTO", color = Blanco, maxLines = 1, softWrap = false, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
    }
}

@Composable
private fun BottomButtonsSection(onPluDirectos: () -> Unit, onComprobantes: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CC)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val buttonModifier = Modifier.weight(1f)
        Button(
            onClick = onPluDirectos,
            modifier = buttonModifier,
            shape = BordeSuave,
            colors = ButtonDefaults.buttonColors(containerColor = Blanco)
        ) { Text("PLU DIRECTOS", color = NegroNexo) }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onComprobantes,
            modifier = buttonModifier,
            shape = BordeSuave,
            colors = ButtonDefaults.buttonColors(containerColor = Blanco)
        ) { Text("COMPROBANTES", color = NegroNexo) }
    }
}

@Composable
private fun ClientListDialog(
    clienteViewModel: ClienteViewModel,
    screenMode: CrudScreenMode,
    onClientSelected: (Cliente) -> Unit,
    onDismiss: () -> Unit,
    onAttemptEdit: (Cliente) -> Unit,
    onDelete: (Cliente) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            tonalElevation = androidx.compose.material3.AlertDialogDefaults.TonalElevation,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            color = Blanco
        ) {
            ClientListContent(
                clienteViewModel = clienteViewModel,
                onClientSelected = onClientSelected,
                onDismiss = onDismiss,
                screenMode = screenMode,
                onAttemptEdit = onAttemptEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun ClientListContent(
    clienteViewModel: ClienteViewModel,
    onClientSelected: (Cliente) -> Unit,
    onDismiss: () -> Unit,
    screenMode: CrudScreenMode,
    onAttemptEdit: (Cliente) -> Unit,
    onDelete: (Cliente) -> Unit
) {
    val pagedClientes = clienteViewModel.pagedClientes.collectAsLazyPagingItems()

    CrudListScreen(
        title = "Clientes",
        items = pagedClientes,
        itemContent = { cliente ->
            val label = buildString {
                append(cliente.nombre ?: "Sin nombre")
                cliente.cuit?.let { cuit -> if (cuit.isNotBlank()) append(" ($cuit)") }
            }
            Text(label)
        },
        onSearchQueryChanged = { query ->
            clienteViewModel.search(query)
        },
        onSelect = { cliente ->
            if (screenMode == CrudScreenMode.VIEW_SELECT) {
                onClientSelected(cliente)
            }
        },
        onDismiss = onDismiss,
        onAttemptEdit = onAttemptEdit,
        onAttemptDelete  = onDelete,
        screenMode = screenMode,
        itemKey = { it.id }
    )
}

@Composable
private fun ProductListContent(
    productoViewModel: ProductoViewModel,
    onProductSelected: (Producto) -> Unit,
    screenMode: CrudScreenMode,
    onDismiss: () -> Unit,
    onAttemptCreate: () -> Unit,
    onAttemptEdit: (Producto) -> Unit,
    onDelete: (Producto) -> Unit
) {
    val pagedProductos = productoViewModel.pagedProductos.collectAsLazyPagingItems()

    CrudListScreen(
        title = "Productos",
        items = pagedProductos,
        itemContent = { producto ->
            val label = buildString {
                append(producto.descripcion ?: "Sin descripción")
                append(" - $${String.format(Locale.getDefault(), "%.2f", producto.precio1)}")
            }
            Text(label)
        },
        onSearchQueryChanged = { query ->
            productoViewModel.search(query)
        },
        onSelect = { producto ->
            if (screenMode == CrudScreenMode.VIEW_SELECT) {
                onProductSelected(producto)
            }
        },
        onDismiss = onDismiss,
        onCreate = if (screenMode == CrudScreenMode.EDIT_DELETE) onAttemptCreate else null,
        onAttemptEdit = onAttemptEdit,
        onAttemptDelete  = onDelete,
        screenMode = screenMode,
        itemKey = { producto ->
            "producto_${producto.localId}"
        }
    )
}

@Composable
fun SyncStatusIcon(state: SyncService.SyncState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state != SyncService.SyncState.IDLE,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val iconRes = when (state) {
            SyncService.SyncState.DOWNLOADING -> R.drawable.ic_download
            SyncService.SyncState.UPLOADING -> R.drawable.ic_upload
            else -> null
        }

        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Estado de Sincronización",
                tint = Blanco,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                    .padding(4.dp)
                    .size(20.dp)
            )
        }
    }
}