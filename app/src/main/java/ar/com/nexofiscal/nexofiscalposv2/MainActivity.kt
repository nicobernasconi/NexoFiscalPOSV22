package ar.com.nexofiscal.nexofiscalposv2

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.*
import ar.com.nexofiscal.nexofiscalposv2.devices.XmlPresentationScreen
import ar.com.nexofiscal.nexofiscalposv2.managers.SyncManager
import ar.com.nexofiscal.nexofiscalposv2.screens.MainScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHelper
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var xmlPresentationScreen: XmlPresentationScreen? = null
    private lateinit var currencyFormat: NumberFormat

    // ViewModels
    private lateinit var productoViewModel: ProductoViewModel
    private lateinit var clienteViewModel: ClienteViewModel
    private lateinit var formaPagoViewModel: FormaPagoViewModel
    private lateinit var tipoViewModel: TipoViewModel
    private lateinit var familiaViewModel: FamiliaViewModel
    private lateinit var tasaIvaViewModel: TasaIvaViewModel
    private lateinit var unidadViewModel: UnidadViewModel
    private lateinit var proveedorViewModel: ProveedorViewModel
    private lateinit var agrupacionViewModel: AgrupacionViewModel
    private lateinit var comprobanteViewModel: ComprobanteViewModel
    private lateinit var renglonComprobanteViewModel: RenglonComprobanteViewModel
    private lateinit var tipoDocumentoViewModel: TipoDocumentoViewModel
    private lateinit var tipoIvaViewModel: TipoIvaViewModel
    private lateinit var categoriaViewModel: CategoriaViewModel
    private lateinit var tipoFormaPagoViewModel: TipoFormaPagoViewModel
    private lateinit var localidadViewModel: LocalidadViewModel
    private lateinit var promocionViewModel: PromocionViewModel
    private lateinit var paisViewModel: PaisViewModel
    private lateinit var provinciaViewModel: ProvinciaViewModel
    private lateinit var rolViewModel: RolViewModel
    private lateinit var sucursalViewModel: SucursalViewModel
    private lateinit var usuarioViewModel: UsuarioViewModel
    private lateinit var vendedorViewModel: VendedorViewModel
    private lateinit var cierreCajaViewModel: CierreCajaViewModel
    private lateinit var tipoComprobanteViewModel: TipoComprobanteViewModel
    private lateinit var configuracionViewModel: ConfiguracionViewModel
    private lateinit var monedaViewModel: MonedaViewModel
    private lateinit var stockViewModel: StockViewModel

    // Listener para cambios en pantallas de presentación
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            Log.i("MainActivity", "Display agregado: $displayId")
            ensureSecondScreenVisible()
        }

        override fun onDisplayRemoved(displayId: Int) {
            Log.i("MainActivity", "Display removido: $displayId")
            xmlPresentationScreen?.dismiss()
            xmlPresentationScreen = null
        }

        override fun onDisplayChanged(displayId: Int) {
            Log.i("MainActivity", "Display cambiado: $displayId")
            ensureSecondScreenVisible()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Eliminado soporte safe area (WindowCompat y colores transparentes)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val prefs = getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
        if (prefs.getBoolean("kiosk_mode_enabled", false)) if (dpm.isLockTaskPermitted(this.packageName)) startLockTask()
        val isOffline = intent.getBooleanExtra("IS_OFFLINE_MODE", false)
        if (isOffline) {
            Handler(Looper.getMainLooper()).postDelayed({
                NotificationManager.show("Sin conexión. Operando en modo offline.", NotificationType.INFO)
            }, 1500)
        }
        NotificationHelper.createNotificationChannel(this)
        observeSyncProgress()
        // Inicializar ViewModels
        productoViewModel = ViewModelProvider(this)[ProductoViewModel::class.java]
        clienteViewModel = ViewModelProvider(this)[ClienteViewModel::class.java]
        formaPagoViewModel = ViewModelProvider(this)[FormaPagoViewModel::class.java]
        tipoViewModel = ViewModelProvider(this)[TipoViewModel::class.java]
        familiaViewModel = ViewModelProvider(this)[FamiliaViewModel::class.java]
        tasaIvaViewModel = ViewModelProvider(this)[TasaIvaViewModel::class.java]
        unidadViewModel = ViewModelProvider(this)[UnidadViewModel::class.java]
        proveedorViewModel = ViewModelProvider(this)[ProveedorViewModel::class.java]
        agrupacionViewModel = ViewModelProvider(this)[AgrupacionViewModel::class.java]
        comprobanteViewModel = ViewModelProvider(this)[ComprobanteViewModel::class.java]
        renglonComprobanteViewModel = ViewModelProvider(this)[RenglonComprobanteViewModel::class.java]
        tipoDocumentoViewModel = ViewModelProvider(this)[TipoDocumentoViewModel::class.java]
        tipoIvaViewModel = ViewModelProvider(this)[TipoIvaViewModel::class.java]
        categoriaViewModel = ViewModelProvider(this)[CategoriaViewModel::class.java]
        tipoFormaPagoViewModel = ViewModelProvider(this)[TipoFormaPagoViewModel::class.java]
        localidadViewModel = ViewModelProvider(this)[LocalidadViewModel::class.java]
        promocionViewModel = ViewModelProvider(this)[PromocionViewModel::class.java]
        paisViewModel = ViewModelProvider(this)[PaisViewModel::class.java]
        provinciaViewModel = ViewModelProvider(this)[ProvinciaViewModel::class.java]
        rolViewModel = ViewModelProvider(this)[RolViewModel::class.java]
        sucursalViewModel = ViewModelProvider(this)[SucursalViewModel::class.java]
        usuarioViewModel = ViewModelProvider(this)[UsuarioViewModel::class.java]
        vendedorViewModel = ViewModelProvider(this)[VendedorViewModel::class.java]
        cierreCajaViewModel = ViewModelProvider(this)[CierreCajaViewModel::class.java]
        tipoComprobanteViewModel = ViewModelProvider(this)[TipoComprobanteViewModel::class.java]
        configuracionViewModel = ViewModelProvider(this)[ConfiguracionViewModel::class.java]
        monedaViewModel = ViewModelProvider(this)[MonedaViewModel::class.java]
        stockViewModel = ViewModelProvider(this)[StockViewModel::class.java]

        currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")).apply {
            maximumFractionDigits = 2; minimumFractionDigits = 2
        }

        setContent {
            NexoFiscalPOSV2Theme {
                BackHandler(true) {
                    NotificationManager.show("El botón de retroceso está deshabilitado. Use el menú para salir.", NotificationType.INFO)
                }
                val isLoading by LoadingManager.isLoading.collectAsState()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize()) {
                        MainScreen(
                            onTotalUpdated = { newTotal -> updateSecondScreenTotal(newTotal) },
                            productoViewModel, clienteViewModel, formaPagoViewModel, tipoViewModel,
                            familiaViewModel, tasaIvaViewModel, unidadViewModel, proveedorViewModel,
                            agrupacionViewModel, comprobanteViewModel, renglonComprobanteViewModel,
                            tipoDocumentoViewModel, tipoIvaViewModel, categoriaViewModel,
                            tipoFormaPagoViewModel, localidadViewModel, promocionViewModel,
                            paisViewModel, provinciaViewModel, rolViewModel, sucursalViewModel, configuracionViewModel,
                            usuarioViewModel, vendedorViewModel, cierreCajaViewModel, tipoComprobanteViewModel, monedaViewModel,
                            stockViewModel
                        )
                        NotificationHost()
                        LoadingDialog(show = isLoading, message = "Sincronizando...")
                    }
                }
            }
        }
        setupSecondScreen()
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        dm.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
    }

    // Asegura que la presentación esté visible si hay una pantalla secundaria disponible
    private fun ensureSecondScreenVisible() {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (presentationDisplays.isNotEmpty()) {
            if (xmlPresentationScreen == null || xmlPresentationScreen?.isShowing != true) {
                Log.i("MainActivity", "(Re)creando pantalla secundaria")
                setupSecondScreen()
            }
        } else {
            if (xmlPresentationScreen != null) {
                Log.i("MainActivity", "No hay presentación disponible, cerrando existente")
                xmlPresentationScreen?.dismiss()
                xmlPresentationScreen = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Revalidar en cada regreso/transition
        ensureSecondScreenVisible()
    }

    private fun observeSyncProgress() {
        lifecycleScope.launch {
            SyncManager.progressState.collectLatest { progress ->
                if (progress.overallTaskIndex > 0) {
                    NotificationHelper.updateSyncNotification(this@MainActivity, progress)
                }
            }
        }
    }

    private fun setupSecondScreen() {
        val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)

        if (presentationDisplays.isNotEmpty()) {
            val display = presentationDisplays[0]
            Log.i("MainActivity", "Segunda pantalla encontrada: $display")
            xmlPresentationScreen = XmlPresentationScreen(this, display).apply {
                setTotal(currencyFormat.format(0.0))
            }
            try {
                xmlPresentationScreen?.show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al mostrar la segunda pantalla XML", e)
                xmlPresentationScreen = null
            }
        } else {
            Log.i("MainActivity", "No se encontraron pantallas de presentación secundarias.")
            xmlPresentationScreen = null
        }
    }

    private fun updateSecondScreenTotal(total: Double) {
        val formattedTotal = currencyFormat.format(total)
        Log.d("MainActivity", "Actualizando total en segunda pantalla XML: $formattedTotal")
        xmlPresentationScreen?.setTotal(formattedTotal)
    }

    override fun onStop() {
        super.onStop()
        // No descartar aquí para mantenerla visible durante transiciones; solo en onDestroy.
        Log.i("MainActivity", "onStop: se mantiene la segunda pantalla activa.")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar listener y cerrar presentación
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        try { dm.unregisterDisplayListener(displayListener) } catch (_: Exception) {}
        xmlPresentationScreen?.dismiss()
        xmlPresentationScreen = null
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Dejar que el SDK/IME gestione todos los eventos de teclado sin interceptar
        return super.dispatchKeyEvent(event)
    }
}