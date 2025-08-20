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
import kotlin.math.abs

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
        row("Ventas Brutas", "$${MoneyUtils.format(resumen.ventasBrutas)}")
        row("Descuentos", "-$${MoneyUtils.format(resumen.descuentos)}")
        row("Notas de Crédito", "-$${MoneyUtils.format(resumen.notasCredito)}")
        // Ventas netas puede ser negativa; manejar signo -$ correctamente
        row("Ventas Netas", (if (resumen.ventasNetas < 0) "-$" + MoneyUtils.format(abs(resumen.ventasNetas)) else "$" + MoneyUtils.format(resumen.ventasNetas)))
        row("IVA Total", "$${MoneyUtils.format(resumen.ivaTotal)}")
        row("Ingresos de Caja", "$${MoneyUtils.format(resumen.movimientosCajaIngreso)}")
        row("Egresos de Caja", "-$${MoneyUtils.format(resumen.movimientosCajaEgreso)}")
        // Gastos (egreso)
        row("Gastos", "-$${MoneyUtils.format(resumen.totalGastos)}")
        row("Total Esperado en Caja", "$${MoneyUtils.format(resumen.totalEsperadoEnCaja)}")
        resumen.efectivoInicial?.let { row("Efectivo Inicial", "$${MoneyUtils.format(it)}") }
        resumen.efectivoFinal?.let { row("Efectivo Final", "$${MoneyUtils.format(it)}") }
        resumen.cierreId?.let { row("ID de Cierre", "$it") }
        resumen.usuarioNombre?.let { row("Usuario", it) }
        val totalPorMedios = resumen.porMedioPago.values.sum()
        row("Total por Medios de Pago", "$${MoneyUtils.format(totalPorMedios)}")

        // Comentarios (opcional, multilínea)
        resumen.comentarios?.takeIf { it.isNotBlank() }?.let { txt ->
            y += bodyPaint.textSize
            canvas.drawText("Comentarios", MARGIN, y, headerPaint); y += headerPaint.textSize * 1.3f
            val maxWidth = (PAGE_WIDTH - 2 * MARGIN)
            val words = txt.split(Regex("\\s+")).filter { it.isNotBlank() }
            var line = StringBuilder()
            fun flushLine() {
                if (line.isNotEmpty()) {
                    val l = line.toString().trim()
                    canvas.drawText(l, MARGIN, y, bodyPaint)
                    y += bodyPaint.textSize * 1.3f
                    line = StringBuilder()
                }
            }
            for (word in words) {
                val candidate = if (line.isEmpty()) word else line.toString() + " " + word
                if (bodyPaint.measureText(candidate) <= maxWidth) {
                    line.clear(); line.append(candidate)
                } else {
                    flushLine()
                    if (bodyPaint.measureText(word) > maxWidth) {
                        var start = 0
                        while (start < word.length) {
                            var end = word.length
                            while (end > start && bodyPaint.measureText(word.substring(start, end)) > maxWidth) {
                                end--
                            }
                            val chunk = word.substring(start, end)
                            canvas.drawText(chunk, MARGIN, y, bodyPaint)
                            y += bodyPaint.textSize * 1.3f
                            start = end
                        }
                    } else {
                        line.append(word)
                    }
                }
                if (y > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN
                }
            }
            flushLine()
        }

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
            row("• $medio", "$${MoneyUtils.format(monto)}")
            if (y > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN
            }
        }
        // Total final del desglose
        y += bodyPaint.textSize
        canvas.drawLine(MARGIN, y, (PAGE_WIDTH - MARGIN), y, headerPaint); y += headerPaint.textSize * 1.2f
        canvas.drawText("TOTAL COBRADO:", (PAGE_WIDTH - MARGIN - 150), y, headerPaint)
        canvas.drawText("$${MoneyUtils.format(totalPorMedios)}", (PAGE_WIDTH - MARGIN), y, TextPaint(headerPaint).apply { textAlign = Paint.Align.RIGHT })

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
}
