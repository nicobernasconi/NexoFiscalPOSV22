package ar.com.nexofiscal.nexofiscalposv2.utils

import android.content.Context
import android.print.PrintManager
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeFiltros
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeResultados
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
                // Intento 1: Impresora térmica
                ticketPrinter.printTicket(comprobante, renglones)
            } catch (e: PrintingException) {
                // Intento 2 (Fallback): Generar PDF para impresión estándar de Android
                Log.w("PrintingManager", "Error de impresora térmica, generando PDF: ${e.message}")
                try {
                    val file = pdfGenerator.createPdfTicket(context, comprobante, renglones)
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "${context.getString(R.string.app_name)} Documento"
                    withContext(Dispatchers.Main) {
                        printManager.print(jobName, PdfPrintDocumentAdapter(context, file), null)
                    }
                } catch (pdfError: Exception) {
                    Log.e("PrintingManager", "Error al generar o imprimir el PDF", pdfError)
                    // Si ambos métodos fallan, lanzamos la excepción final.
                    throw PrintingException("La impresora no está disponible y falló la generación del PDF.")
                }
            }
        }
    }
    suspend fun printInforme(
        context: Context,
        filtros: InformeFiltros,
        resultados: InformeResultados
    ) {
        if (resultados.comprobantes.isEmpty()) {
            NotificationManager.show("No hay datos para imprimir en el informe.", NotificationType.WARNING)
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // 1. Generar el archivo PDF del informe.
                val pdfGenerator = InformePdfGenerator()
                val file = pdfGenerator.createPdfInforme(context, filtros, resultados)

                // 2. Usar el PrintManager de Android para imprimir/guardar el PDF.
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Informe de Ventas"

                // 3. Volver al hilo principal para mostrar la UI de impresión.
                withContext(Dispatchers.Main) {
                    printManager.print(jobName, PdfPrintDocumentAdapter(context, file), null)
                }

            } catch (e: Exception) {
                Log.e("PrintingManager", "Error al generar o imprimir el PDF del informe", e)
                withContext(Dispatchers.Main) {
                    NotificationManager.show("Error al crear el PDF del informe.", NotificationType.ERROR)
                }
            }
        }
    }
}