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
    // Para tabla con bordes: suma columnas + 4 bordes (|...|...|...|) = 32 -> columnas internas = 28
    private val QTY_COL = 4
    private val DESC_COL = 16
    private val SUBT_COL = 8

    private fun padRight(text: String, width: Int): String {
        val t = if (text.length > width) text.substring(0, width) else text
        return t + " ".repeat((width - t.length).coerceAtLeast(0))
    }

    private fun padLeft(text: String, width: Int): String {
        val t = if (text.length > width) text.takeLast(width) else text
        return " ".repeat((width - t.length).coerceAtLeast(0)) + t
    }

    @Suppress("SameParameterValue")
    private fun wrapByWidth(text: String, maxWidth: Int): List<String> {
        if (text.length <= maxWidth) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (w in words) {
            if (current.isEmpty()) {
                if (w.length <= maxWidth) current.append(w) else lines.add(w.substring(0, maxWidth))
            } else {
                if (current.length + 1 + w.length <= maxWidth) {
                    current.append(' ').append(w)
                } else {
                    lines.add(current.toString())
                    current = StringBuilder()
                    if (w.length <= maxWidth) current.append(w) else lines.add(w.substring(0, maxWidth))
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    // --- Tabla ASCII ---
    private fun borderLine(): String = "+" + "-".repeat(QTY_COL) + "+" + "-".repeat(DESC_COL) + "+" + "-".repeat(SUBT_COL) + "+"
    private fun rowLine(qty: String, desc: String, subtotal: String): String {
        val q = padLeft(qty, QTY_COL)
        val d = padRight(desc, DESC_COL)
        val s = padLeft(subtotal, SUBT_COL)
        return "|$q|$d|$s|"
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
            val tipoTexto = when (comprobante.tipoComprobanteId) {
                1 -> "FACTURA B"; 3 -> "PEDIDO X"; 2 -> "PRESUPUESTO X"; 4 -> "NOTA DE CREDITO X";5->"COMPROBANTE DE CAJA";6->"ACOPIO X";7->"DESACOPIO X" else -> "TICKET"
            }
            printer!!.setPrintAppendString(tipoTexto, format)
            val pv = SessionManager.puntoVentaNumero
            format.apply { textSize = 26; style = NORMAL }
            printer!!.setPrintAppendString(String.format("P.V.: %05d   Nro.: %08d", pv, comprobante.numeroFactura ?: 0), format)
            printer!!.setPrintAppendString("Fecha: ${comprobante.fecha}", format)
            printDivider()

            // --- Tabla de ítems ---
            format.apply { textSize = 24; style = NORMAL; ali = Layout.Alignment.ALIGN_NORMAL }
            // Borde superior
            printer!!.setPrintAppendString(borderLine(), format)
            // Encabezado
            val header = rowLine("Cant", "Descripción", "Subtotal")
            printer!!.setPrintAppendString(header, format)
            // Separador de encabezado
            printer!!.setPrintAppendString(borderLine(), format)

            var totalCalculado = 0.0
            renglones.forEach { renglon ->
                val subtotalValue = renglon.totalLinea.toDoubleOrNull() ?: 0.0
                totalCalculado += subtotalValue

                val qtyStr = formatQuantity(renglon.cantidad)
                val subtotalStr = String.format(Locale.US, "$%.2f", subtotalValue)

                val descLines = wrapByWidth(renglon.descripcion, DESC_COL)
                descLines.forEachIndexed { idx, line ->
                    val q = if (idx == 0) qtyStr else ""
                    val s = if (idx == 0) subtotalStr else ""
                    printer!!.setPrintAppendString(rowLine(q, line, s), format)
                }
            }
            // Borde inferior de la tabla
            printer!!.setPrintAppendString(borderLine(), format)
            printDivider()

            val total = comprobante.total?.toDoubleOrNull() ?: totalCalculado
            val ivaTotal = comprobante.importeIva ?: 0.0
            val totalSinIva = total - ivaTotal

            // --- Totales alineados derecha ---
            format.apply { textSize = 28; style = NORMAL; ali = Layout.Alignment.ALIGN_NORMAL }
            val lineTotalSinIva = padRight("Total s/IVA:", LINE_WIDTH - SUBT_COL) + padLeft(String.format(Locale.US, "$%.2f", totalSinIva), SUBT_COL)
            val lineIva = padRight("Total IVA:", LINE_WIDTH - SUBT_COL) + padLeft(String.format(Locale.US, "$%.2f", ivaTotal), SUBT_COL)
            val lineTotal = padRight("TOTAL:", LINE_WIDTH - SUBT_COL) + padLeft(String.format(Locale.US, "$%.2f", total), SUBT_COL)
            printer!!.setPrintAppendString(lineTotalSinIva, format)
            printer!!.setPrintAppendString(lineIva, format)
            printer!!.setPrintAppendString(lineTotal, format)

            val cae = comprobante.cae ?: ""
            format.apply { textSize = 30; style = NORMAL; ali = Layout.Alignment.ALIGN_CENTER }
            if (!comprobante.qr.isNullOrBlank() && comprobante.tipoComprobanteId == 1) {
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


            printer!!.setPrintAppendString("\n\n\n\n", format)
            val result = printer!!.setPrintStart()
            if (result != 0) throw PrintingException("La impresora reportó un error. Código: $result")

        } catch (e: Exception) {
            Log.e("TicketPrinter", "Error durante el proceso de impresión", e)
            throw PrintingException(e.message ?: "Error desconocido en la impresora")
        }
    }
}