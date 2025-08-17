package ar.com.nexofiscal.nexofiscalposv2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import android.util.Base64
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class PdfTicketGenerator {

    private val TICKET_WIDTH = 226
    private val MARGIN = 10f
    private val TICKET_HEIGHT = 2000

    @SuppressLint("DefaultLocale")
    fun createPdfTicket(
        context: Context,
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(TICKET_WIDTH, TICKET_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = MARGIN + 10

        // Estilos
        val monoTypeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        val monoBoldTypeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val centerPaint = TextPaint().apply { typeface = monoBoldTypeface; textSize = 12f; textAlign = Paint.Align.CENTER }
        val normalCenterPaint = TextPaint(centerPaint).apply { typeface = monoTypeface; textSize = 9f }
        val boldCenterPaint = TextPaint(centerPaint).apply { textSize = 9f }
        val leftPaint = TextPaint(normalCenterPaint).apply { textAlign = Paint.Align.LEFT; textSize = 9f; typeface = monoTypeface }
        val leftBoldPaint = TextPaint(leftPaint).apply { typeface = monoBoldTypeface }
        val largeBoldCenterPaint = TextPaint(centerPaint).apply { textSize = 16f }

        // Encabezado (logo + datos empresa)
        yPosition += drawLogo(canvas, SessionManager.empresaLogoBase64, TICKET_WIDTH / 2f, yPosition)
        yPosition += drawText(canvas, SessionManager.empresaNombre ?: "Empresa", TICKET_WIDTH / 2f, yPosition, centerPaint.apply { textSize = 14f }) + 5
        yPosition += drawText(canvas, "${SessionManager.empresaDireccion ?: "Dirección"}\nBuenos Aires, Argentina\n", TICKET_WIDTH / 2f, yPosition, normalCenterPaint) + 5
        yPosition += drawText(canvas, "C.U.I.T.: ${SessionManager.empresaCuit ?: ""}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "Razón Social: ${SessionManager.empresaRazonSocial ?: ""}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "Atiende: ${SessionManager.nombreCompleto ?: "Usuario"}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "I.I.B.B.: ${SessionManager.empresaIIBB ?: ""}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "Inicio Act.: ${SessionManager.empresaInicioActividades ?: ""}\n", TICKET_WIDTH / 2f, yPosition, boldCenterPaint) + 5
        yPosition += drawDivider(canvas, yPosition) + 5

        // Cliente
        yPosition += drawText(canvas, "Cliente: ${comprobante.cliente?.nombre ?: "CONSUMIDOR FINAL"}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        comprobante.cliente?.cuit?.let { yPosition += drawText(canvas, "CUIT: $it", TICKET_WIDTH / 2f, yPosition, boldCenterPaint) }
        yPosition += drawDivider(canvas, yPosition) + 5

        // Tipo de comprobante (igual a TicketPrinter)
        val tipoTexto = when (comprobante.tipoComprobanteId) {
            1 -> "FACTURA B"
            3 -> "PEDIDO X"
            2 -> "PRESUPUESTO X"
            4 -> "NOTA DE CREDITO X"
            5 -> "COMPROBANTE DE CAJA"
            6 -> "ACOPIO X"
            7 -> "DESACOPIO X"
            else -> "TICKET"
        }
        yPosition += drawText(canvas, tipoTexto, TICKET_WIDTH / 2f, yPosition, largeBoldCenterPaint) + 5
        yPosition += drawText(canvas, String.format("P.V.: %05d   Nro.: %08d", SessionManager.puntoVentaNumero, comprobante.numeroFactura ?: 0), TICKET_WIDTH / 2f, yPosition, normalCenterPaint)
        yPosition += drawText(canvas, "Fecha: ${comprobante.fecha}\n", TICKET_WIDTH / 2f, yPosition, normalCenterPaint) + 5
        yPosition += drawDivider(canvas, yPosition) + 5

        // Cabecera de ítems (mismo ancho: 4, 20, 8)
        yPosition += drawText(canvas, String.format("%-4s%-20s%8s", "Cant", "Producto", "Subtotal"), MARGIN, yPosition, leftBoldPaint) + 2
        yPosition += drawDivider(canvas, yPosition) + 8

        // Renglones
        renglones.forEach { renglon ->
            val subtotal = renglon.totalLinea.toDoubleOrNull() ?: 0.0
            val line = String.format(Locale.US, "%-4s%-20s%8s", formatQuantity(renglon.cantidad), (renglon.descripcion ?: "").take(20), String.format("$%.2f", subtotal))
            yPosition += drawText(canvas, line, MARGIN, yPosition, leftPaint)
        }
        yPosition += drawDivider(canvas, yPosition) + 8

        // Totales
        val total = comprobante.total?.toDoubleOrNull() ?: 0.0
        val ivaTotal = comprobante.importeIva ?: 0.0
        val totalSinIva = total - ivaTotal
        yPosition += drawText(canvas, String.format("%-16s%16s", "Total s/IVA:", String.format("$%.2f", totalSinIva)), MARGIN, yPosition, leftPaint)
        yPosition += drawText(canvas, String.format("%-16s%16s", "Total IVA:", String.format("$%.2f", ivaTotal)), MARGIN, yPosition, leftPaint)
        yPosition += drawText(canvas, String.format("%-16s%16s", "TOTAL:", String.format("$%.2f", total)), MARGIN, yPosition, leftBoldPaint) + 15

        // CAE / QR o mensaje de no validez (igual a TicketPrinter)
        if (!comprobante.qr.isNullOrBlank() && comprobante.tipoComprobanteId == 1) {
            yPosition += drawText(canvas, "CAE: ${comprobante.cae ?: ""}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint.apply { textSize = 9f }) + 3
            yPosition += drawText(canvas, "Vencimiento: ${comprobante.fechaVencimiento ?: ""}", TICKET_WIDTH / 2f, yPosition, boldCenterPaint.apply { textSize = 9f }) + 5
            try {
                val qrBitmap = QrCodeGenerator.generateQrBitmap(comprobante.qr!!, 120, 120)
                if (qrBitmap != null) {
                    val qrX = (TICKET_WIDTH / 2f) - (qrBitmap.width / 2f)
                    canvas.drawBitmap(qrBitmap, qrX, yPosition, null)
                    yPosition += qrBitmap.height + 10
                }
            } catch (e: Exception) {
                Log.e("PdfTicketGenerator", "Fallo al generar o dibujar el QR en el PDF.", e)
            }
            yPosition += drawText(canvas, "Régimen de Transparencia Fiscal al Consumidor Ley 27.743", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        } else {
            yPosition += drawText(canvas, "Este comprobante no tiena validez fiscal", TICKET_WIDTH / 2f, yPosition, boldCenterPaint)
        }

        document.finishPage(page)

        // Guardar archivo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Comprobante_${timeStamp}.pdf"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(downloadsDir, fileName)
        try {
            FileOutputStream(file).use { document.writeTo(it) }
        } catch (e: IOException) {
            Log.e("PdfTicketGenerator", "Error al escribir el PDF", e)
            throw e
        } finally {
            document.close()
        }
        return file
    }

    @SuppressLint("DefaultLocale")
    private fun formatQuantity(qty: Double): String {
        return if (qty % 1 == 0.0) String.format("%d", qty.toLong()) else DecimalFormat("#.###").format(qty)
    }

    private fun drawText(canvas: android.graphics.Canvas, text: String, x: Float, y: Float, paint: TextPaint): Float {
        var yPos = y
        for (line in text.split("\n")) {
            canvas.drawText(line, x, yPos, paint)
            yPos += paint.descent() - paint.ascent()
        }
        return yPos - y
    }

    private fun drawDivider(canvas: android.graphics.Canvas, y: Float): Float {
        canvas.drawText("--------------------------------", TICKET_WIDTH / 2f, y, TextPaint().apply { textAlign = Paint.Align.CENTER; textSize = 9f })
        return 10f
    }

    private fun drawLogo(canvas: android.graphics.Canvas, base64String: String?, x: Float, y: Float): Float {
        base64String?.takeIf { it.isNotBlank() }?.let { raw ->
            try {
                val b64 = if (raw.startsWith("data:image")) raw.substringAfter(',') else raw
                val decodedBytes = Base64.decode(b64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val logoWidth = 80f
                    val logoHeight = logoWidth / aspectRatio
                    val logoX = x - (logoWidth / 2)
                    canvas.drawBitmap(bitmap, null, android.graphics.RectF(logoX, y, logoX + logoWidth, y + logoHeight), null)
                    return logoHeight + 5
                }
            } catch (e: Exception) {
                Log.w("PdfTicketGenerator", "Error al decodificar/dibujar logo: ${e.message}")
            }
        }
        return 0f
    }
}