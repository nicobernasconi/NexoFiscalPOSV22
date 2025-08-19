package ar.com.nexofiscal.nexofiscalposv2.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaFiltros
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaResumen
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CierreCajaPdfGenerator {

    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN = 40f

    fun createPdfCierreCaja(
        context: Context,
        filtros: CierreCajaFiltros,
        resumen: CierreCajaResumen
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }
        val headerPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
            color = Color.BLACK
        }
        val bodyPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = Color.DKGRAY
        }
        val rightAlign = Paint().apply { textAlign = Paint.Align.RIGHT }

        var y = MARGIN

        // Cabecera
        canvas.drawText("Cierre de Caja", MARGIN, y, titlePaint)
        y += titlePaint.textSize * 2
        val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Empresa: ${SessionManager.empresaNombre ?: "N/A"}", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 1.3f
        canvas.drawText("Emitido: $nowStr", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 2

        // Filtros
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Filtros", MARGIN, y, headerPaint); y += headerPaint.textSize * 1.5f
        filtros.desde?.let { canvas.drawText("• Desde: ${df.format(it)}", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 1.2f }
        filtros.hasta?.let { canvas.drawText("• Hasta: ${df.format(it)}", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 1.2f }
        filtros.puntoVenta?.let { canvas.drawText("• Punto de Venta: $it", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 1.2f }
        filtros.usuario?.let { canvas.drawText("• Usuario: $it", MARGIN, y, bodyPaint); y += bodyPaint.textSize * 1.2f }
        y += headerPaint.textSize * 1.2f

        // Resumen de totales
        canvas.drawText("Resumen", MARGIN, y, headerPaint); y += headerPaint.textSize * 1.5f
        fun row(label: String, value: String) {
            canvas.drawText(label, MARGIN, y, bodyPaint)
            canvas.drawText(value, (PAGE_WIDTH - MARGIN), y, TextPaint(bodyPaint).apply { textAlign = Paint.Align.RIGHT })
            y += bodyPaint.textSize * 1.4f
        }
        row("Ventas Brutas", "$${format(resumen.ventasBrutas)}")
        row("Descuentos", "-$${format(resumen.descuentos)}")
        row("Notas de Crédito", "-$${format(resumen.notasCredito)}")
        row("Ventas Netas", "$${format(resumen.ventasNetas)}")
        row("IVA Total", "$${format(resumen.ivaTotal)}")
        row("Ingresos de Caja", "$${format(resumen.movimientosCajaIngreso)}")
        row("Egresos de Caja", "-$${format(resumen.movimientosCajaEgreso)}")
        row("Total Esperado en Caja", "$${format(resumen.totalEsperadoEnCaja)}")
        resumen.efectivoInicial?.let { row("Efectivo Inicial", "$${format(it)}") }
        resumen.efectivoFinal?.let { row("Efectivo Final", "$${format(it)}") }
        resumen.cierreId?.let { row("ID de Cierre", "$it") }
        resumen.usuarioNombre?.let { row("Usuario", it) }
        // NUEVO: total por medios de pago en el resumen
        val totalPorMedios = resumen.porMedioPago.values.sum()
        row("Total por Medios de Pago", "$${format(totalPorMedios)}")

        y += bodyPaint.textSize
        canvas.drawLine(MARGIN, y, (PAGE_WIDTH - MARGIN), y, headerPaint); y += headerPaint.textSize

        // Cantidades
        row("Comprobantes", "${resumen.cantidadComprobantes}")
        row("Notas de Crédito", "${resumen.cantidadNC}")
        row("Cancelados", "${resumen.cancelados}")

        y += headerPaint.textSize
        // Desglose por medio de pago
        canvas.drawText("Por Medio de Pago", MARGIN, y, headerPaint); y += headerPaint.textSize * 1.5f
        for ((medio, monto) in resumen.porMedioPago) {
            row("• $medio", "$${format(monto)}")
            if (y > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN
            }
        }
        // NUEVO: total al final del desglose
        y += bodyPaint.textSize
        canvas.drawLine(MARGIN, y, (PAGE_WIDTH - MARGIN), y, headerPaint); y += headerPaint.textSize * 1.2f
        canvas.drawText("TOTAL COBRADO:", (PAGE_WIDTH - MARGIN - 150), y, headerPaint)
        canvas.drawText("$${format(totalPorMedios)}", (PAGE_WIDTH - MARGIN), y, TextPaint(headerPaint).apply { textAlign = Paint.Align.RIGHT })

        document.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Cierre_Caja_${timeStamp}.pdf"
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, fileName)
        try {
            FileOutputStream(file).use { document.writeTo(it) }
        } catch (e: IOException) {
            throw IOException("Error al escribir el archivo PDF: ${e.message}")
        } finally {
            document.close()
        }
        return file
    }

    private fun format(value: Double): String = String.format(Locale.getDefault(), "%.2f", value)
}
