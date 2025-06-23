// main/java/ar/com/nexofiscal/nexofiscalposv2/utils/QrCodeGenerator.kt
package ar.com.nexofiscal.nexofiscalposv2.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

/**
 * Utilidad para generar códigos QR como Bitmaps usando la librería ZXing.
 * NOTA: Requiere la dependencia 'com.google.zxing:core'.
 */
object QrCodeGenerator {
    private const val TAG = "QrGenerator"

    /**
     * Codifica un texto en un Bitmap de código QR.
     * @param text El contenido a codificar.
     * @param width El ancho del bitmap resultante.
     * @param height La altura del bitmap resultante.
     * @return Un Bitmap con el QR, o null si ocurre un error.
     */
    fun generateQrBitmap(text: String, width: Int, height: Int): Bitmap? {
        if (text.isBlank()) {
            Log.w(TAG, "El texto para el QR está vacío.")
            return null
        }
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar el bitmap del QR", e)
            null
        }
    }
}