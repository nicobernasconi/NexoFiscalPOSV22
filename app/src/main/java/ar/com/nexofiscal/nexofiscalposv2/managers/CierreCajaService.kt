package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object CierreCajaService {

    data class CierrePdfResult(
        val file: File,
        val pageCount: Int
    )

    // Texto listo para enviar a la impresora térmica
    suspend fun generarTextoImpresion(context: Context, cierreId: Int): String {
        val db = AppDatabase.getInstance(context)
        val informe = db.cierreCajaDao().getInformeById(cierreId)
            ?: throw IllegalArgumentException("Cierre no encontrado: $cierreId")

        val nf = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val sb = StringBuilder()
        sb.appendLine("CIERRE DE CAJA #${informe.id}")
        sb.appendLine("Fecha: ${informe.fecha ?: "-"}")
        sb.appendLine("Usuario: ${informe.nombreUsuario ?: informe.usuarioId ?: "-"}")
        sb.appendLine("Efectivo inicial: ${nf.format(informe.efectivoInicial ?: 0.0)}")
        sb.appendLine("Efectivo final:   ${nf.format(informe.efectivoFinal ?: 0.0)}")
        sb.appendLine("")
        sb.appendLine("Formas de pago:")

        val ventasJson = try { JSONObject(informe.ventasPorFormaPago) } catch (_: Exception) { JSONObject() }
        val pagosItems = ventasJson.keys().asSequence().map { k -> k to ventasJson.optDouble(k, 0.0) }
            .sortedByDescending { it.second }.toList()

        if (pagosItems.isEmpty()) sb.appendLine("(sin pagos)") else pagosItems.forEach { (nombre, total) ->
            sb.appendLine("- ${nombre}: ${nf.format(total)}")
        }
        sb.appendLine("")
        sb.appendLine("Total ventas: ${nf.format(informe.totalVentas ?: 0.0)}")

        // Gastos
        sb.appendLine("")
        sb.appendLine("Gastos por tipo:")
        val gastosJson = try { JSONObject(informe.gastosPorTipo) } catch (_: Exception) { JSONObject() }
        val gastosItems = gastosJson.keys().asSequence().map { k -> k to gastosJson.optDouble(k, 0.0) }
            .sortedByDescending { it.second }.toList()
        if (gastosItems.isEmpty()) sb.appendLine("(sin gastos)") else gastosItems.forEach { (nombre, total) ->
            sb.appendLine("- ${nombre}: ${nf.format(total)}")
        }
        sb.appendLine("Total gastos: ${nf.format(informe.totalGastos ?: 0.0)}")

        // Neto
        val neto = (informe.totalVentas ?: 0.0) - (informe.totalGastos ?: 0.0)
        sb.appendLine("")
        sb.appendLine("Neto (Ventas - Gastos): ${nf.format(neto)}")

        if (!informe.comentarios.isNullOrBlank()) {
            sb.appendLine("")
            sb.appendLine("Notas: ${informe.comentarios}")
        }
        return sb.toString()
    }

    // PDF como respaldo cuando no hay impresora
    suspend fun generarInformeCierreEnPdf(context: Context, cierreId: Int): CierrePdfResult {
        val db = AppDatabase.getInstance(context)
        val informe = db.cierreCajaDao().getInformeById(cierreId)
            ?: throw IllegalArgumentException("Cierre no encontrado: $cierreId")

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 18f
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 12f
        }

        var y = 40f
        val margin = 40f
        val nf = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

        // Encabezado
        canvas.drawText("Cierre de Caja #${informe.id}", margin, y, titlePaint)
        y += 22f
        canvas.drawText("Fecha: ${informe.fecha ?: "-"}", margin, y, textPaint)
        y += 16f
        canvas.drawText("Usuario: ${informe.nombreUsuario ?: informe.usuarioId ?: "-"}", margin, y, textPaint)
        y += 16f
        canvas.drawText("Efectivo inicial: ${nf.format(informe.efectivoInicial ?: 0.0)}", margin, y, textPaint)
        y += 16f
        canvas.drawText("Efectivo final: ${nf.format(informe.efectivoFinal ?: 0.0)}", margin, y, textPaint)
        y += 24f

        // Desglose por Forma de Pago
        canvas.drawText("Formas de pago:", margin, y, boldPaint)
        y += 18f
        val ventasJson = try { JSONObject(informe.ventasPorFormaPago) } catch (_: Exception) { JSONObject() }
        val pagosItems = ventasJson.keys().asSequence().map { k -> k to ventasJson.optDouble(k, 0.0) }
            .sortedByDescending { it.second }.toList()

        if (pagosItems.isEmpty()) {
            canvas.drawText("(sin pagos)", margin, y, textPaint)
            y += 16f
        } else {
            pagosItems.forEach { (nombre, total) ->
                canvas.drawText("- ${nombre}", margin, y, textPaint)
                val amountText = nf.format(total)
                val amountX = pageInfo.pageWidth - margin - textPaint.measureText(amountText)
                canvas.drawText(amountText, amountX, y, textPaint)
                y += 16f
                if (y > pageInfo.pageHeight - 60) {
                    pdf.finishPage(page)
                    val nextInfo = PdfDocument.PageInfo.Builder(595, 842, pdf.pages.size + 1).create()
                    val nextPage = pdf.startPage(nextInfo)
                    // Reiniciar referencias a nueva página
                    val nextCanvas = nextPage.canvas
                    y = 40f
                    nextCanvas.drawText("Cierre #${informe.id} (cont.)", margin, y, textPaint)
                    y += 20f
                }
            }
        }

        // Totales de ventas
        y += 8f
        canvas.drawText("Total ventas: ${nf.format(informe.totalVentas ?: 0.0)}", margin, y, boldPaint)
        y += 22f

        // Desglose de Gastos por tipo
        canvas.drawText("Gastos por tipo:", margin, y, boldPaint)
        y += 18f
        val gastosJson = try { JSONObject(informe.gastosPorTipo) } catch (_: Exception) { JSONObject() }
        val gastosItems = gastosJson.keys().asSequence().map { k -> k to gastosJson.optDouble(k, 0.0) }
            .sortedByDescending { it.second }.toList()
        if (gastosItems.isEmpty()) {
            canvas.drawText("(sin gastos)", margin, y, textPaint)
            y += 16f
        } else {
            gastosItems.forEach { (nombre, total) ->
                canvas.drawText("- ${nombre}", margin, y, textPaint)
                val amountText = nf.format(total)
                val amountX = pageInfo.pageWidth - margin - textPaint.measureText(amountText)
                canvas.drawText(amountText, amountX, y, textPaint)
                y += 16f
                if (y > pageInfo.pageHeight - 60) {
                    pdf.finishPage(page)
                    val nextInfo = PdfDocument.PageInfo.Builder(595, 842, pdf.pages.size + 1).create()
                    val nextPage = pdf.startPage(nextInfo)
                    val nextCanvas = nextPage.canvas
                    y = 40f
                    nextCanvas.drawText("Cierre #${informe.id} (cont.)", margin, y, textPaint)
                    y += 20f
                }
            }
        }

        // Total gastos y Neto
        y += 8f
        canvas.drawText("Total gastos: ${nf.format(informe.totalGastos ?: 0.0)}", margin, y, boldPaint)
        y += 16f
        val neto = (informe.totalVentas ?: 0.0) - (informe.totalGastos ?: 0.0)
        canvas.drawText("Neto (Ventas - Gastos): ${nf.format(neto)}", margin, y, boldPaint)

        pdf.finishPage(page)

        val outDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val dir = File(outDir, "cierres")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "cierre_${informe.id}.pdf")
        FileOutputStream(file).use { fos -> pdf.writeTo(fos) }
        pdf.close()

        return CierrePdfResult(file = file, pageCount = pdf.pages.size)
    }
}
