// src/main/java/ar/com/nexofiscal/nexofiscalposv2/utils/TicketPrinter.kt
package ar.com.nexofiscal.nexofiscalposv2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.text.Layout
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import com.zcs.sdk.DriverManager
import com.zcs.sdk.Printer
import com.zcs.sdk.SdkResult
import com.zcs.sdk.Sys
import com.zcs.sdk.pin.pinpad.PinPadManager
import com.zcs.sdk.print.PrnStrFormat
import com.zcs.sdk.print.PrnTextFont.CUSTOM
import com.zcs.sdk.print.PrnTextStyle
import com.zcs.sdk.print.PrnTextStyle.BOLD
import com.zcs.sdk.print.PrnTextStyle.NORMAL

class TicketPrinter(context: Context) {

    private val sysDevice: Sys
    private val pinPad: PinPadManager
    private val printer: Printer
    private val prefs: SharedPreferences

    init {
        val driver = DriverManager.getInstance()
        sysDevice = driver.getBaseSysDevice()
        pinPad    = driver.getPadManager()
        printer   = driver.getPrinter()
        prefs     = context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
    }

    /**
     * Imprime un ticket con los productos vendidos y el cliente.
     *
     * @param context           contexto de la Activity
     * @param productos         lista de productos vendidos
     * @param cliente           cliente seleccionado (o null para Consumidor Final)
     * @param tipoComprobante   1=Factura B, 2=Pedido, otro=Presupuesto
     */
    @SuppressLint("DefaultLocale")
    fun printTicket(
        context: Context,
        productos: List<Producto>,
        cliente: Cliente?,
        tipoComprobante: Int
    ) {
        // Validaciones
        if (productos.isEmpty()) {
            android.widget.Toast.makeText(context, "No hay productos para imprimir", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (printer.getPrinterStatus() == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
            android.widget.Toast.makeText(context, "Impresora sin papel", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Formato base
        val format = PrnStrFormat().apply {
            textSize = 36
            style    = BOLD
            font     = CUSTOM
            ali      = Layout.Alignment.ALIGN_CENTER
        }

        // 1) Logo
        prefs.getString("empresa_logo", "")?.takeIf { it.isNotBlank() }?.let { raw ->
            var b64 = raw
            if (b64.startsWith("data:image")) b64 = b64.substringAfter(',').trim()
            try {
                val decoded = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)?.let { bmp ->
                    printer.setPrintAppendBitmap(bmp, Layout.Alignment.ALIGN_CENTER)
                }
            } catch (e: Exception) {
                Log.w("TicketPrinter", "Logo decode error: ${e.message}")
            }
        }

        // 2) Datos de empresa y usuario
        val nombre    = prefs.getString("empresa_nombre", "Empresa")!!
        val direccion = prefs.getString("empresa_direccion", "")!!
        val cuit      = prefs.getString("empresa_cuit", "")!!
        val razSoc    = prefs.getString("empresa_razon_social", "")!!
        val iibb      = prefs.getString("empresa_iibb", "")!!
        val inicio    = prefs.getString("empresa_fecha_inicio_actividades", "")!!
        val usuario   = prefs.getString("nombre_completo", "Usuario")!!

        printer.setPrintAppendString(nombre, format)
        format.apply { textSize = 25; style = NORMAL }
        printer.setPrintAppendString("$direccion\nBuenos Aires, Argentina\n", format)
        format.apply { textSize = 30; style = BOLD }
        printer.setPrintAppendString("C.U.I.T.: $cuit", format)
        printer.setPrintAppendString("Razón Social: $razSoc", format)
        printer.setPrintAppendString("Atiende: $usuario", format)
        printer.setPrintAppendString("I.I.B.B.: $iibb", format)
        printer.setPrintAppendString("Inicio Act.: $inicio\n", format)
        printer.setPrintAppendString("--------------------------------", format)

        // 3) Cliente
        format.apply { textSize = 30; style = BOLD; ali = Layout.Alignment.ALIGN_CENTER }
        if (cliente != null) {
            printer.setPrintAppendString("Cliente: ${cliente.nombre}", format)
            cliente.cuit?.let { printer.setPrintAppendString("CUIT: $it", format) }
        } else {
            printer.setPrintAppendString("CONSUMIDOR FINAL", format)
        }
        printer.setPrintAppendString("--------------------------------", format)

        // 4) Tipo comprobante
        format.apply { textSize = 50; style = BOLD }
        val tipoTexto = when (tipoComprobante) {
            1 -> "FACTURA B"
            2 -> "PEDIDO X"
            else -> "PRESUPUESTO X"
        }
        printer.setPrintAppendString(tipoTexto, format)
        val pv  = prefs.getInt("punto_venta_numero", 1)
        val nro = prefs.getInt("ultimo_numero_comprobante", 1)
        format.apply { textSize = 30; style = NORMAL }
        printer.setPrintAppendString(String.format("P.V.: %05d   Nro.: %08d", pv, nro), format)
        printer.setPrintAppendString("Fecha: ${android.text.format.DateFormat.format("dd/MM/yyyy", System.currentTimeMillis())}\n", format)
        printer.setPrintAppendString("--------------------------------", format)

        // 5) Listado de productos y cálculo
        format.apply { textSize = 29; style = NORMAL; ali = Layout.Alignment.ALIGN_NORMAL }
        var total = 0.0
        var iva21 = 0.0
        var iva105 = 0.0
        var iva0 = 0.0

        // Cabecera columnas
        printer.setPrintAppendString(String.format("%-3s%-20s%8s\n", "CANT", "DESCRIPCIÓN", "SUBT"), format)
        printer.setPrintAppendString("--------------------------------\n", format)

        productos.forEach { p ->
            val qty      = 1
            val unit     = p.precio1
            val sub      = qty * unit
            total += sub

            when (p.tasaIva?.tasa) {
                0.105 -> iva105 += sub - (sub / 1.105)
                0.0   -> iva0   += 0.0
                else  -> iva21  += sub - (sub / 1.21)
            }

            val descCorta = p.descripcion.orEmpty().padEnd(20).substring(0,20)
            printer.setPrintAppendString(
                String.format("%-3d%-20s%8s\n", qty, descCorta, String.format("$%.2f", sub)),
                format
            )
        }

        // 6) Totales IVA y total
        printer.setPrintAppendString("--------------------------------", format)
        val ivaTotal = iva21 + iva105 + iva0
        val sinIva   = total - ivaTotal
        if (tipoComprobante == 1) {
            printer.setPrintAppendString(String.format("IVA 21%%: %8s\n", String.format("$%.2f", iva21)), format)
            printer.setPrintAppendString(String.format("IVA 10.5%%:%8s\n", String.format("$%.2f", iva105)), format)
            printer.setPrintAppendString(String.format("IVA 0%%:  %8s\n", String.format("$%.2f", iva0)), format)
            printer.setPrintAppendString(String.format("Sin IVA:%8s\n", String.format("$%.2f", sinIva)), format)
        }

        format.style = BOLD
        printer.setPrintAppendString(String.format("TOTAL:  %8s\n", String.format("$%.2f", total)), format)

        // 7) Leyenda fiscal / QR
        if (tipoComprobante == 1) {
            format.apply { textSize = 29; style = NORMAL; ali = Layout.Alignment.ALIGN_CENTER }
            printer.setPrintAppendString("--------------------------------", format)
            printer.setPrintAppendString("CAE: 123456789\n", format)
            printer.setPrintAppendQRCode("NexoFiscal", 400, 400, Layout.Alignment.ALIGN_CENTER)
            printer.setPrintAppendString("\nRégimen de Transparencia Fiscal\n", format)
        } else {
            format.apply { textSize = 24; ali = Layout.Alignment.ALIGN_CENTER }
            printer.setPrintAppendString("Este comprobante no tiene validez fiscal.\n", format)
        }

        // 8) Enviar a la impresora
        printer.setPrintStart()
    }
}
