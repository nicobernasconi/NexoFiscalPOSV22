package ar.com.nexofiscal.nexofiscalposv2.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeFiltros
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeResultados
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class InformePdfGenerator {

    // Constantes para un formato A4 (en puntos)
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN = 40f

    fun createPdfInforme(
        context: Context,
        filtros: InformeFiltros,
        resultados: InformeResultados
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // --- Estilos de Texto ---
        val titlePaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }
        val headerPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            color = Color.BLACK
        }
        val bodyPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = Color.DKGRAY
        }
        val totalPaint = TextPaint(headerPaint).apply { textSize = 14f }

        var yPos = MARGIN

        // --- Cabecera del Documento ---
        canvas.drawText("Informe de Ventas", MARGIN, yPos, titlePaint)
        yPos += titlePaint.textSize * 2

        canvas.drawText("Empresa: ${SessionManager.empresaNombre ?: "N/A"}", MARGIN, yPos, bodyPaint)
        yPos += bodyPaint.textSize * 1.5f
        canvas.drawText("Fecha de Emisión: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", MARGIN, yPos, bodyPaint)
        yPos += bodyPaint.textSize * 2

        // --- Sección de Filtros Aplicados ---
        canvas.drawText("Filtros Aplicados:", MARGIN, yPos, headerPaint)
        yPos += headerPaint.textSize * 1.5f
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        filtros.fechaDesde?.let { canvas.drawText("• Desde: ${dateFormat.format(it)}", MARGIN, yPos, bodyPaint); yPos += bodyPaint.textSize * 1.2f }
        filtros.fechaHasta?.let { canvas.drawText("• Hasta: ${dateFormat.format(it)}", MARGIN, yPos, bodyPaint); yPos += bodyPaint.textSize * 1.2f }
        filtros.tipoComprobante?.let { canvas.drawText("• Tipo Comprobante: ${it.nombre}", MARGIN, yPos, bodyPaint); yPos += bodyPaint.textSize * 1.2f }
        filtros.cliente?.let { canvas.drawText("• Cliente: ${it.nombre}", MARGIN, yPos, bodyPaint); yPos += bodyPaint.textSize * 1.2f }
        filtros.vendedor?.let { canvas.drawText("• Vendedor: ${it.nombre}", MARGIN, yPos, bodyPaint); yPos += bodyPaint.textSize * 1.2f }
        yPos += headerPaint.textSize * 2

        // --- Cabecera de la Tabla de Resultados ---
        canvas.drawText("Fecha", MARGIN, yPos, headerPaint)
        canvas.drawText("Tipo/Nro", MARGIN + 80, yPos, headerPaint)
        canvas.drawText("Cliente", MARGIN + 220, yPos, headerPaint)
        canvas.drawText("Monto", PAGE_WIDTH - MARGIN - 50, yPos, headerPaint.apply { textAlign = Paint.Align.RIGHT })
        yPos += headerPaint.textSize
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, headerPaint)
        yPos += headerPaint.textSize * 1.5f

        // --- Filas de la Tabla ---
        for (detalle in resultados.comprobantes) {
            val comprobante = detalle.comprobante
            val fecha = comprobante.fecha?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.let { d -> dateFormat.format(d) } } ?: "N/A"
            val tipoNum = "${detalle.tipoComprobante?.nombre ?: "CPN"} Nº ${comprobante.numeroFactura ?: ""}"
            val cliente = (detalle.cliente?.nombre ?: "Consumidor Final").take(30)
            val monto = "$${comprobante.total}"

            canvas.drawText(fecha, MARGIN, yPos, bodyPaint)
            canvas.drawText(tipoNum, MARGIN + 80, yPos, bodyPaint)
            canvas.drawText(cliente, MARGIN + 220, yPos, bodyPaint)
            canvas.drawText(monto, PAGE_WIDTH - MARGIN, yPos, bodyPaint.apply { textAlign = Paint.Align.RIGHT })
            yPos += bodyPaint.textSize * 1.5f

            // Lógica para cambio de página si el contenido excede la altura
            if (yPos > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                val newPage = document.startPage(pageInfo)
                yPos = MARGIN
                // Podrías repetir la cabecera aquí si lo deseas
            }
        }

        // --- Línea y Total Final ---
        yPos += totalPaint.textSize
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, headerPaint)
        yPos += totalPaint.textSize * 1.5f

        canvas.drawText("TOTAL VENTAS:", PAGE_WIDTH - MARGIN - 150, yPos, totalPaint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", resultados.totalVentas)}", PAGE_WIDTH - MARGIN, yPos, totalPaint.apply { textAlign = Paint.Align.RIGHT })

        document.finishPage(page)

        // --- Guardar el Archivo ---
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Informe_Ventas_${timeStamp}.pdf"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(downloadsDir, fileName)

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