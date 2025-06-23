package ar.com.nexofiscal.nexofiscalposv2.utils

import android.content.Context
import android.print.PrintManager
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrintingManager {

    suspend fun print(
        context: Context,
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>
    ) {
        withContext(Dispatchers.IO) {
            val ticketPrinter = TicketPrinter()
            val pdfGenerator = PdfTicketGenerator()

            try {
                ticketPrinter.printTicket(comprobante, renglones)
                withContext(Dispatchers.Main) {
                    NotificationManager.show("Comprobante enviado a la impresora.", NotificationType.SUCCESS)
                }
            } catch (e: PrintingException) {
                Log.w("PrintingManager", "Error de impresora térmica, generando PDF: ${e.message}")
                withContext(Dispatchers.Main) {
                    NotificationManager.show("Impresora no detectada. Abriendo vista de impresión...", NotificationType.INFO)
                }
                try {
                    // --- CAMBIO: La llamada ahora es correcta ---
                    val file = pdfGenerator.createPdfTicket(context, comprobante, renglones)

                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "${context.getString(R.string.app_name)} Documento"
                    withContext(Dispatchers.Main) {
                        printManager.print(jobName, PdfPrintDocumentAdapter(context, file), null)
                    }
                } catch (pdfError: Exception) {
                    Log.e("PrintingManager", "Error al generar o imprimir el PDF", pdfError)
                    withContext(Dispatchers.Main) {
                        NotificationManager.show("Error al crear el PDF.", NotificationType.ERROR)
                    }
                }
            }
        }
    }
}