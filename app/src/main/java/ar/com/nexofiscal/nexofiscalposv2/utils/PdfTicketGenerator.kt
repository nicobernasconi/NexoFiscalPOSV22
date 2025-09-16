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

    private val PAGE_WIDTH_MM = 58f
    private val POINTS_PER_INCH = 72f
    private val MM_PER_INCH = 25.4f
    private val PAGE_WIDTH_POINTS = ((PAGE_WIDTH_MM / MM_PER_INCH) * POINTS_PER_INCH).toInt() // ≈165
    private val MARGIN = 8f
    private val BASE_HEIGHT = 1200 // se ajusta dinámicamente posteriormente

    @SuppressLint("DefaultLocale")
    fun createPdfTicket(
        context: Context,
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>
    ): File {
        val pageWidth = PAGE_WIDTH_POINTS
        val dynamicHeight = BASE_HEIGHT + (renglones.size * 38) + if (comprobante.tipoComprobanteId == 4) 260 else 0
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, dynamicHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = MARGIN + 6

        // Estilos (ligeramente reducidos para 58mm)
        val monoTypeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        val monoBoldTypeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val centerPaint = TextPaint().apply { typeface = monoBoldTypeface; textSize = 11f; textAlign = Paint.Align.CENTER }
        val normalCenterPaint = TextPaint(centerPaint).apply { typeface = monoTypeface; textSize = 8f }
        val boldCenterPaint = TextPaint(centerPaint).apply { textSize = 8f }
        val leftPaint = TextPaint(normalCenterPaint).apply { textAlign = Paint.Align.LEFT; textSize = 8f; typeface = monoTypeface }
        val leftBoldPaint = TextPaint(leftPaint).apply { typeface = monoBoldTypeface }
        val largeBoldCenterPaint = TextPaint(centerPaint).apply { textSize = 14f }

        val centerX = pageWidth / 2f

        // Encabezado
        yPosition += drawLogo(canvas, SessionManager.empresaLogoBase64, centerX, yPosition, pageWidth)
        yPosition += drawText(canvas, SessionManager.empresaNombre ?: "Empresa", centerX, yPosition, centerPaint) + 2
        yPosition += drawText(canvas, (SessionManager.empresaDireccion ?: "Dirección") + "\nBs. As. - Argentina\n", centerX, yPosition, normalCenterPaint) + 2
        yPosition += drawText(canvas, "CUIT: ${SessionManager.empresaCuit ?: ""}", centerX, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "RS: ${SessionManager.empresaRazonSocial ?: ""}", centerX, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "Atiende: ${SessionManager.nombreCompleto ?: "Usuario"}", centerX, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "IIBB: ${SessionManager.empresaIIBB ?: ""}", centerX, yPosition, boldCenterPaint)
        yPosition += drawText(canvas, "Inicio: ${SessionManager.empresaInicioActividades ?: ""}\n", centerX, yPosition, boldCenterPaint) + 2
        yPosition += drawDivider(canvas, yPosition, pageWidth) + 2

        // Cliente
        yPosition += drawText(canvas, "Cliente: ${comprobante.cliente?.nombre ?: "CONSUMIDOR FINAL"}", centerX, yPosition, boldCenterPaint)
        comprobante.cliente?.cuit?.let { yPosition += drawText(canvas, "CUIT: $it", centerX, yPosition, boldCenterPaint) }
        yPosition += drawDivider(canvas, yPosition, pageWidth) + 2

        val tipoTexto = when (comprobante.tipoComprobanteId) {
            1 -> "FACTURA ${((comprobante.letra ?: "").trim().uppercase()).ifBlank { "B" }}"
            4 -> "NC ${((comprobante.letra ?: "").trim().uppercase()).ifBlank { "X" }}"
            3 -> "PEDIDO X"
            2 -> "PRESU X"
            5 -> "CAJA"
            6 -> "ACOPIO"
            7 -> "DESACOP"
            else -> "TICKET"
        }
        yPosition += drawText(canvas, tipoTexto, centerX, yPosition, largeBoldCenterPaint) + 2
        val numeroParaImprimir = when (comprobante.tipoComprobanteId) {
            3 -> (comprobante.numero ?: 0)
            else -> (comprobante.numeroFactura ?: comprobante.numero ?: 0)
        }
        yPosition += drawText(canvas, String.format("PV:%05d Nro:%08d", SessionManager.puntoVentaNumero, numeroParaImprimir), centerX, yPosition, normalCenterPaint)
        yPosition += drawText(canvas, "Fecha: ${comprobante.fecha}\n", centerX, yPosition, normalCenterPaint) + 2
        yPosition += drawDivider(canvas, yPosition, pageWidth) + 2

        // Cabecera ítems (ajustar longitud de producto a 18)
        yPosition += drawText(canvas, String.format("%-4s%-18s%7s", "Cant", "Producto", "Subt"), MARGIN, yPosition, leftBoldPaint) + 1
        yPosition += drawDivider(canvas, yPosition, pageWidth) + 4

        // Renglones
        renglones.forEach { renglon ->
            val subtotal = renglon.totalLinea.toDoubleOrNull() ?: 0.0
            val line = String.format(Locale.US, "%-4s%-18s%7s", formatQuantity(renglon.cantidad), renglon.descripcion.take(18), String.format("$%.2f", subtotal))
            yPosition += drawText(canvas, line, MARGIN, yPosition, leftPaint)
        }
        yPosition += drawDivider(canvas, yPosition, pageWidth) + 4

        val total = comprobante.total?.toDoubleOrNull() ?: 0.0
        val ivaTotal = comprobante.importeIva ?: 0.0
        val totalSinIva = total - ivaTotal
        yPosition += drawText(canvas, String.format("%-13s%10s", "Total s/IVA:", String.format("$%.2f", totalSinIva)), MARGIN, yPosition, leftPaint)
        yPosition += drawText(canvas, String.format("%-13s%10s", "Total IVA:", String.format("$%.2f", ivaTotal)), MARGIN, yPosition, leftPaint)
        yPosition += drawText(canvas, String.format("%-13s%10s", "TOTAL:", String.format("$%.2f", total)), MARGIN, yPosition, leftBoldPaint) + 10

        val mostrarQr = !comprobante.qr.isNullOrBlank() && (comprobante.tipoComprobanteId == 1 || comprobante.tipoComprobanteId == 4)
        if (mostrarQr) {
            yPosition += drawText(canvas, "CAE: ${comprobante.cae ?: ""}", centerX, yPosition, boldCenterPaint) + 2
            yPosition += drawText(canvas, "Vto: ${comprobante.fechaVencimiento ?: ""}", centerX, yPosition, boldCenterPaint) + 4
            try {
                comprobante.qr?.let { qrData ->
                    val side = (pageWidth * 0.55f).toInt().coerceAtLeast(80)
                    val qrBitmap = QrCodeGenerator.generateQrBitmap(qrData, side, side)
                    if (qrBitmap != null) {
                        val qrX = centerX - (qrBitmap.width / 2f)
                        canvas.drawBitmap(qrBitmap, qrX, yPosition, null)
                        yPosition += qrBitmap.height + 6
                    }
                }
            } catch (e: Exception) {
                Log.e("PdfTicketGenerator", "Fallo QR NC/PDF: ${e.message}")
            }
            yPosition += drawText(canvas, "Transparencia Fiscal Ley 27.743", centerX, yPosition, boldCenterPaint)
        } else {
            yPosition += drawText(canvas, "Sin validez fiscal", centerX, yPosition, boldCenterPaint)
        }

        if (comprobante.tipoComprobanteId == 4) {
            yPosition += 16
            val firmaPaint = TextPaint(leftPaint).apply { textSize = 8f }
            yPosition += drawText(canvas, "--------------------------------", MARGIN, yPosition, firmaPaint)
            yPosition += drawText(canvas, "Firma: _____________________", MARGIN, yPosition, firmaPaint)
            yPosition += drawText(canvas, "Aclaración: ________________", MARGIN, yPosition, firmaPaint)
            yPosition += drawText(canvas, "DNI: _______________________", MARGIN, yPosition, firmaPaint)
        }

        document.finishPage(page)

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

    private fun drawDivider(canvas: android.graphics.Canvas, y: Float, width: Int): Float {
        canvas.drawText("-".repeat(30), width / 2f, y, TextPaint().apply { textAlign = Paint.Align.CENTER; textSize = 8f })
        return 8f
    }

    private fun drawLogo(canvas: android.graphics.Canvas, base64String: String?, x: Float, y: Float, pageWidth: Int): Float {
        base64String?.takeIf { it.isNotBlank() }?.let { raw ->
            try {
                val b64 = if (raw.startsWith("data:image")) raw.substringAfter(',') else raw
                val decodedBytes = Base64.decode(b64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val logoWidth = pageWidth * 0.45f
                    val logoHeight = logoWidth / aspectRatio
                    val logoX = x - (logoWidth / 2)
                    canvas.drawBitmap(bitmap, null, android.graphics.RectF(logoX, y, logoX + logoWidth, y + logoHeight), null)
                    return logoHeight + 4
                }
            } catch (e: Exception) {
                Log.w("PdfTicketGenerator", "Error logo: ${e.message}")
            }
        }
        return 0f
    }
}