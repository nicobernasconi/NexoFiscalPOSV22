package ar.com.nexofiscal.nexofiscalposv2.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.text.Layout
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import com.zcs.sdk.DriverManager
import com.zcs.sdk.Printer
import com.zcs.sdk.SdkResult
import com.zcs.sdk.print.PrnStrFormat
import com.zcs.sdk.print.PrnTextFont.CUSTOM
import com.zcs.sdk.print.PrnTextStyle.BOLD
import com.zcs.sdk.print.PrnTextStyle.NORMAL
import java.text.DecimalFormat
import java.util.Locale

class PrintingException(message: String) : Exception(message)

class TicketPrinter {
    private var printer: Printer? = null
    private val isPrinterAvailable: Boolean

    init {
        isPrinterAvailable = try {
            printer = DriverManager.getInstance().printer
            Log.i("TicketPrinter", "ZCS Printer SDK inicializada correctamente.")
            true
        } catch (e: Throwable) {
            Log.e("TicketPrinter", "Error al inicializar ZCS Printer SDK.", e)
            false
        }
    }

    private fun formatQuantity(qty: Double): String {
        return if (qty % 1 == 0.0) String.format(Locale.US, "%d", qty.toLong()) else DecimalFormat("#.###").format(qty)
    }

    // --- Helpers de formato ---
    private val LINE_WIDTH = 32
    private val SUBT_COL = 8 // ancho para valores numéricos a la derecha

    // Restaura función kvRight para alinear clave-valor a derecha
    private fun kvRight(left: String, right: String): String {
        val leftTrim = if (left.length >= LINE_WIDTH) left.substring(0, LINE_WIDTH - 1) else left
        val rightTrim = if (right.length > SUBT_COL) right.takeLast(SUBT_COL) else right
        val spaces = (LINE_WIDTH - leftTrim.length - rightTrim.length).coerceAtLeast(1)
        return leftTrim + " ".repeat(spaces) + rightTrim
    }

    @SuppressLint("DefaultLocale")
    fun printTicket(comprobante: Comprobante, renglones: List<RenglonComprobante>) {
        if (!isPrinterAvailable || printer == null) throw PrintingException("El hardware de la impresora no está disponible.")
        if (renglones.isEmpty()) throw PrintingException("No hay productos para imprimir.")
        if (printer!!.getPrinterStatus() == SdkResult.SDK_PRN_STATUS_PAPEROUT) throw PrintingException("La impresora no tiene papel.")

        try {
            val format = PrnStrFormat().apply {
                textSize = 24
                style = NORMAL
                font = CUSTOM
                ali = Layout.Alignment.ALIGN_CENTER
            }
            fun printDivider() {
                format.ali = Layout.Alignment.ALIGN_CENTER
                printer!!.setPrintAppendString("-".repeat(LINE_WIDTH), format)
            }

            // --- CAMBIO: Datos obtenidos del SessionManager ---
            SessionManager.empresaLogoBase64?.takeIf { it.isNotBlank() }?.let { raw ->
                try {
                    val b64 = if (raw.startsWith("data:image")) raw.substringAfter(',') else raw
                    val decoded = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decoded, 0, decoded.size)?.let { printer!!.setPrintAppendBitmap(it, Layout.Alignment.ALIGN_CENTER) }
                } catch (e: Exception) { Log.w("TicketPrinter", "Error en logo: ${e.message}") }
            }

            val nombre = SessionManager.empresaNombre ?: "Empresa"
            val direccion = SessionManager.empresaDireccion ?: ""
            val cuit = SessionManager.empresaCuit ?: ""
            val razSoc = SessionManager.empresaRazonSocial ?: ""
            val iibb = SessionManager.empresaIIBB ?: ""
            val inicio = SessionManager.empresaInicioActividades ?: ""
            val usuario = SessionManager.nombreCompleto ?: "Usuario"

            // --- El resto del ticket ahora usa los objetos `comprobante` y `renglones` ---
            format.apply { textSize = 30; style = BOLD }; printer!!.setPrintAppendString(nombre, format)
            format.apply { textSize = 24; style = NORMAL }; printer!!.setPrintAppendString(direccion, format)
            printer!!.setPrintAppendString("Buenos Aires, Argentina", format)
            format.apply { textSize = 26; style = BOLD }; printer!!.setPrintAppendString("C.U.I.T.: $cuit", format)
            printer!!.setPrintAppendString("Razón Social: $razSoc", format)
            printer!!.setPrintAppendString("Atiende: $usuario", format)
            printer!!.setPrintAppendString("I.I.B.B.: $iibb", format)
            printer!!.setPrintAppendString("Inicio Act.: $inicio", format)
            printDivider()

            format.apply { textSize = 26; style = BOLD; ali = Layout.Alignment.ALIGN_CENTER }
            printer!!.setPrintAppendString("Cliente: ${comprobante.cliente?.nombre ?: "CONSUMIDOR FINAL"}", format)
            comprobante.cliente?.cuit?.let { printer!!.setPrintAppendString("CUIT: $it", format) }
            printDivider()

            format.apply { textSize = 42; style = BOLD }
            val letra = (comprobante.letra ?: "").trim().uppercase()
            val tipoTexto = when (comprobante.tipoComprobanteId) {
                1 -> "FACTURA ${if (letra.isNotBlank()) letra else "B"}"
                4 -> "NOTA DE CREDITO ${if (letra.isNotBlank()) letra else "X"}"
                3 -> "PEDIDO X"
                2 -> "PRESUPUESTO X"
                5 -> "COMPROBANTE DE CAJA"
                6 -> "ACOPIO X"
                7 -> "DESACOPIO X"
                else -> "TICKET"
            }
            printer!!.setPrintAppendString(tipoTexto, format)
            val pv = SessionManager.puntoVentaNumero
            // NUEVO: número mostrado según tipo (PEDIDO usa 'numero'; resto usa numeroFactura o fallback a numero)
            val numeroParaImprimir = when (comprobante.tipoComprobanteId) {
                3 -> (comprobante.numero ?: 0) // Pedido
                else -> (comprobante.numeroFactura ?: comprobante.numero ?: 0)
            }
            format.apply { textSize = 26; style = NORMAL }
            printer!!.setPrintAppendString(String.format("P.V.: %05d   Nro.: %08d", pv, numeroParaImprimir), format)
            printer!!.setPrintAppendString("Fecha: ${comprobante.fecha}", format)
            printDivider()

            // --- Ítems unificados con formato PDF (Cant, Producto, Subtotal) ---
            printDivider()
            format.apply { textSize = 24; style = BOLD; ali = Layout.Alignment.ALIGN_NORMAL }
            printer!!.setPrintAppendString(String.format("%-4s%-20s%8s", "Cant", "Producto", "Subtotal"), format)
            format.apply { style = NORMAL }
            printer!!.setPrintAppendString("-".repeat(LINE_WIDTH), format)

            var totalCalculado = 0.0
            renglones.forEach { renglon ->
                val subtotalValue = renglon.totalLinea.toDoubleOrNull() ?: 0.0
                totalCalculado += subtotalValue
                val qtyStr = formatQuantity(renglon.cantidad)
                val desc = renglon.descripcion.take(20)
                val line = String.format(Locale.US, "%-4s%-20s%8s", qtyStr, desc, String.format("$%.2f", subtotalValue))
                printer!!.setPrintAppendString(line, format)
            }
            printer!!.setPrintAppendString("-".repeat(LINE_WIDTH), format)

            val total = comprobante.total?.toDoubleOrNull() ?: totalCalculado
            val ivaTotal = comprobante.importeIva ?: 0.0
            val totalSinIva = total - ivaTotal

            // Totales alineados tipo tabla
            format.apply { textSize = 24; style = NORMAL }
            printer!!.setPrintAppendString(kvRight("Total s/IVA:", String.format(Locale.US, "$%.2f", totalSinIva)), format)
            printer!!.setPrintAppendString(kvRight("Total IVA:", String.format(Locale.US, "$%.2f", ivaTotal)), format)
            format.apply { style = BOLD; textSize = 28 }
            printer!!.setPrintAppendString(kvRight("TOTAL:", String.format(Locale.US, "$%.2f", total)), format)

            val cae = comprobante.cae ?: ""
            format.apply { textSize = 30; style = NORMAL; ali = Layout.Alignment.ALIGN_CENTER }
            val esFiscalConQr = !comprobante.qr.isNullOrBlank() && (comprobante.tipoComprobanteId == 1 || comprobante.tipoComprobanteId == 4)
            if (esFiscalConQr) {
                printer!!.setPrintAppendString("\n\n", format)
                printer!!.setPrintAppendString("CAE: $cae", format)
                printer!!.setPrintAppendString("Vencimiento: ${comprobante.fechaVencimiento ?: ""}", format)
                try {
                    val qrBitmap = QrCodeGenerator.generateQrBitmap(comprobante.qr, 400, 400)
                    if (qrBitmap != null) {
                        printer!!.setPrintAppendBitmap(qrBitmap, Layout.Alignment.ALIGN_CENTER)
                    }
                } catch (e: Exception) {
                    Log.e("TicketPrinter", "Fallo al generar o imprimir el QR.", e)
                }
                format.apply { style = BOLD; textSize = 23 }
                printer!!.setPrintAppendString("Régimen de Transparencia Fiscal al Consumidor Ley 27.743", format)
            } else {
                printer!!.setPrintAppendString("Este comprobante no tiena validez fiscal", format)
            }

            // Bloque de Firma solo para Nota de Crédito
            if (comprobante.tipoComprobanteId == 4) {
                format.apply { style = NORMAL; textSize = 24; ali = Layout.Alignment.ALIGN_NORMAL }
                printer!!.setPrintAppendString("\n\n--------------------------------", format)
                printer!!.setPrintAppendString("Firma: _________________________", format)
                printer!!.setPrintAppendString("Aclaración: ____________________", format)
                printer!!.setPrintAppendString("DNI: ___________________________", format)
            }

            printer!!.setPrintAppendString("\n\n\n\n", format)
            val result = printer!!.setPrintStart()
            if (result != 0) throw PrintingException("La impresora reportó un error. Código: $result")

        } catch (e: Exception) {
            Log.e("TicketPrinter", "Error durante el proceso de impresión", e)
            throw PrintingException(e.message ?: "Error desconocido en la impresora")
        }
    }
}