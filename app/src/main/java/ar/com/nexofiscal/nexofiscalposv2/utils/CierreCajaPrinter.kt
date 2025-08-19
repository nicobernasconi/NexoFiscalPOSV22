package ar.com.nexofiscal.nexofiscalposv2.utils

import android.text.Layout
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaFiltros
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaResumen
import com.zcs.sdk.DriverManager
import com.zcs.sdk.Printer
import com.zcs.sdk.SdkResult
import com.zcs.sdk.print.PrnStrFormat
import com.zcs.sdk.print.PrnTextStyle.BOLD
import com.zcs.sdk.print.PrnTextStyle.NORMAL
import java.util.Locale

class CierreCajaPrinter {
    private var printer: Printer? = null
    private val isPrinterAvailable: Boolean

    init {
        isPrinterAvailable = try {
            printer = DriverManager.getInstance().printer
            true
        } catch (e: Throwable) {
            Log.e("CierreCajaPrinter", "Error inicializando impresora", e)
            false
        }
    }

    private val LINE_WIDTH = 32
    private val SUBT_COL = 12

    private fun padRight(text: String, width: Int): String {
        val t = if (text.length > width) text.substring(0, width) else text
        return t + " ".repeat((width - t.length).coerceAtLeast(0))
    }

    private fun padLeft(text: String, width: Int): String {
        val t = if (text.length > width) text.takeLast(width) else text
        return " ".repeat((width - t.length).coerceAtLeast(0)) + t
    }

    private fun kvRight(left: String, right: String): String {
        val leftTrim = if (left.length >= LINE_WIDTH) left.substring(0, LINE_WIDTH - 1) else left
        val rightTrim = if (right.length > SUBT_COL) right.takeLast(SUBT_COL) else right
        val spaces = (LINE_WIDTH - leftTrim.length - rightTrim.length).coerceAtLeast(1)
        return leftTrim + " ".repeat(spaces) + rightTrim
    }

    fun print(filtros: CierreCajaFiltros, resumen: CierreCajaResumen) {
        if (!isPrinterAvailable || printer == null) throw PrintingException("El hardware de la impresora no está disponible.")
        if (printer!!.getPrinterStatus() == SdkResult.SDK_PRN_STATUS_PAPEROUT) throw PrintingException("La impresora no tiene papel.")

        try {
            val format = PrnStrFormat().apply {
                textSize = 24
                style = NORMAL
                ali = Layout.Alignment.ALIGN_CENTER
            }

            fun divider() { printer!!.setPrintAppendString("-".repeat(LINE_WIDTH), format) }

            // Encabezado con datos de la empresa
            val nombre = SessionManager.empresaNombre ?: "Empresa"
            val direccion = SessionManager.empresaDireccion ?: ""
            val cuit = SessionManager.empresaCuit ?: ""

            format.apply { textSize = 30; style = BOLD }
            printer!!.setPrintAppendString(nombre, format)
            format.apply { textSize = 24; style = NORMAL }
            if (direccion.isNotBlank()) printer!!.setPrintAppendString(direccion, format)
            if (cuit.isNotBlank()) printer!!.setPrintAppendString("C.U.I.T.: $cuit", format)
            divider()

            // Título
            format.apply { textSize = 36; style = BOLD }
            printer!!.setPrintAppendString("CIERRE DE CAJA", format)
            format.apply { textSize = 24; style = NORMAL }

            // Info general de cierre
            val usuario = resumen.usuarioNombre ?: (filtros.usuario ?: SessionManager.nombreCompleto ?: "Usuario")
            val pv = SessionManager.puntoVentaNumero
            val cierreIdStr = resumen.cierreId?.let { "#${String.format(Locale.US, "%06d", it)}" } ?: ""
            printer!!.setPrintAppendString("Usuario: $usuario", format)
            pv?.let { printer!!.setPrintAppendString("Pto. Venta: ${String.format(Locale.US, "%05d", it)} $cierreIdStr", format) }
            divider()

            // Valores de efectivo
            val efIni = resumen.efectivoInicial ?: 0.0
            val efFin = resumen.efectivoFinal ?: 0.0
            val dif = efFin - efIni
            format.ali = Layout.Alignment.ALIGN_NORMAL
            printer!!.setPrintAppendString(kvRight("Efectivo inicial:", String.format(Locale.US, "$%.2f", efIni)), format)
            printer!!.setPrintAppendString(kvRight("Efectivo final:", String.format(Locale.US, "$%.2f", efFin)), format)
            printer!!.setPrintAppendString(kvRight("Diferencia:", String.format(Locale.US, "$%.2f", dif)), format)
            divider()

            // Resumen de comprobantes
            val cant = resumen.cantidadComprobantes
            val canc = resumen.cancelados
            val cantNc = resumen.cantidadNC
            printer!!.setPrintAppendString(kvRight("Comprobantes:", "$cant"), format)
            printer!!.setPrintAppendString(kvRight("Cancelados:", "$canc"), format)
            printer!!.setPrintAppendString(kvRight("Notas de crédito:", "$cantNc"), format)
            divider()

            // Totales
            printer!!.setPrintAppendString(kvRight("Ventas brutas:", String.format(Locale.US, "$%.2f", resumen.ventasBrutas)), format)
            printer!!.setPrintAppendString(kvRight("Descuentos:", String.format(Locale.US, "$%.2f", resumen.descuentos)), format)
            printer!!.setPrintAppendString(kvRight("Notas de crédito:", String.format(Locale.US, "$%.2f", resumen.notasCredito)), format)
            printer!!.setPrintAppendString(kvRight("IVA total:", String.format(Locale.US, "$%.2f", resumen.ivaTotal)), format)
            format.apply { style = BOLD }
            printer!!.setPrintAppendString(kvRight("Ventas netas:", String.format(Locale.US, "$%.2f", resumen.ventasNetas)), format)
            format.style = NORMAL
            divider()

            // Resumen por formas de pago
            format.apply { textSize = 28; style = BOLD; ali = Layout.Alignment.ALIGN_CENTER }
            printer!!.setPrintAppendString("Formas de pago", format)
            divider()
            format.apply { textSize = 24; style = NORMAL; ali = Layout.Alignment.ALIGN_NORMAL }

            var totalPagos = 0.0
            resumen.porMedioPago.toSortedMap().forEach { (nombrePago, importe) ->
                totalPagos += importe
                printer!!.setPrintAppendString(kvRight(nombrePago, String.format(Locale.US, "$%.2f", importe)), format)
            }
            divider()
            format.style = BOLD
            printer!!.setPrintAppendString(kvRight("Total cobrado:", String.format(Locale.US, "$%.2f", totalPagos)), format)
            format.style = NORMAL

            // Final
            format.apply { ali = Layout.Alignment.ALIGN_CENTER }
            printer!!.setPrintAppendString("\n\n", format)
            val result = printer!!.setPrintStart()
            if (result != 0) throw PrintingException("La impresora reportó un error. Código: $result")
        } catch (e: Exception) {
            Log.e("CierreCajaPrinter", "Error imprimiendo cierre de caja", e)
            throw PrintingException(e.message ?: "Error de impresión de cierre de caja")
        }
    }
}
