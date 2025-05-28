// nexofiscalposv2/devices/PresentationHelper.kt
package ar.com.nexofiscal.nexofiscalposv2.devices

import android.app.Presentation
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64 // Importar Base64
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ar.com.nexofiscal.nexofiscalposv2.R // Asegúrate que R se importa correctamente

class XmlPresentationScreen(
    private val parentActivityContext: Context,
    display: Display
) : Presentation(parentActivityContext, display) {

    private var textViewTotal: TextView? = null
    private var imageViewLogo: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_screen_layout) // Carga el layout XML

        textViewTotal = findViewById(R.id.second_screen_total)
        imageViewLogo = findViewById(R.id.second_screen_logo)


    }

    fun setTotal(totalString: String) {
        val plain = totalString.replace("$", "").replace(",", "").trim()
        val mostrar = try {
            plain.toDouble().let { it != 0.0 }
        } catch (e: Exception) {
            false
        }

        if (mostrar) {
            textViewTotal?.text = totalString
            textViewTotal?.visibility = View.VISIBLE
        } else {
            textViewTotal?.visibility = View.GONE // Oculta el TextView si el total es $0,00
        }
    }
}