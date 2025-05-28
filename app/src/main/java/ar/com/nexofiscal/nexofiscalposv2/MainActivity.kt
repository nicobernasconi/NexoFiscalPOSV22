package ar.com.nexofiscal.nexofiscalposv2

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import ar.com.nexofiscal.nexofiscalposv2.devices.XmlPresentationScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.MainScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    // private var composeSecondScreen: ComposeSecondScreen? = null // Comenta o elimina esta línea
    private var xmlPresentationScreen: XmlPresentationScreen? = null // Nueva variable
    private lateinit var currencyFormat: NumberFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

        setContent {
            NexoFiscalPOSV2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize()) {
                        MainScreen(
                            onTotalUpdated = { newTotal ->
                                // Esta lambda se llamará desde MainScreen cuando el total cambie
                                updateSecondScreenTotal(newTotal)
                            }
                        )
                        NotificationHost()    // el host de notificaciones “flota” arriba
                    }
                }
            }
        }
        setupSecondScreen()
    }

    private fun setupSecondScreen() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)

        if (presentationDisplays.isNotEmpty()) {
            val display = presentationDisplays[0]
            Log.i("MainActivity", "Segunda pantalla encontrada: $display")
            // Instancia la nueva clase
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

    fun updateSecondScreenTotal(total: Double) {
        val formattedTotal = currencyFormat.format(total)
        Log.d("MainActivity", "Actualizando total en segunda pantalla XML: $formattedTotal")
        xmlPresentationScreen?.setTotal(formattedTotal) // Llama al método de la nueva clase
    }

    override fun onStop() {
        super.onStop()
        xmlPresentationScreen?.dismiss()
        xmlPresentationScreen = null
        Log.i("MainActivity", "Segunda pantalla XML liberada en onStop.")
    }

    override fun onDestroy() {
        super.onDestroy()
        xmlPresentationScreen?.dismiss()
        xmlPresentationScreen = null
    }
}
