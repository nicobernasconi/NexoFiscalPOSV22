package ar.com.nexofiscal.nexofiscalposv2.utils

import android.content.Context
import android.print.PrintManager
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeFiltros
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.InformeResultados
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import com.google.gson.Gson
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

            // Nueva lógica: si es Nota de Crédito, intentar usar comprobanteIdBaja (serverId original)
            val esNotaCredito = (comprobante.tipoFactura in listOf(3,8,13,53)) || comprobante.tipoComprobanteId == 4
            val renglonesEfectivos: List<RenglonComprobante> = if (esNotaCredito && !comprobante.comprobanteIdBaja.isNullOrBlank()) {
                val originalServerId = comprobante.comprobanteIdBaja.toIntOrNull()
                if (originalServerId != null) {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val originalEntity = db.comprobanteDao().getByServerId(originalServerId)
                        if (originalEntity != null) {
                            val originales = db.renglonComprobanteDao().getByComprobanteId(originalEntity.id)
                            if (originales.isNotEmpty()) {
                                Log.d("PrintingManager", "NC: usando ${originales.size} renglones del comprobante original serverId=$originalServerId localId=${originalEntity.id}")
                                val gson = Gson()
                                originales.map { gson.fromJson(it.data, RenglonComprobante::class.java) }
                            } else {
                                Log.w("PrintingManager", "NC: comprobante original sin renglones, uso renglones provistos (${renglones.size})")
                                renglones
                            }
                        } else {
                            Log.w("PrintingManager", "NC: no se encontró comprobante original serverId=$originalServerId, uso renglones provistos (${renglones.size})")
                            renglones
                        }
                    } catch (e: Exception) {
                        Log.e("PrintingManager", "Error obteniendo renglones originales para NC: ${e.message}")
                        renglones
                    }
                } else {
                    Log.w("PrintingManager", "NC: comprobanteIdBaja no es numérico (${comprobante.comprobanteIdBaja}), uso renglones provistos (${renglones.size})")
                    renglones
                }
            } else renglones

            try {
                // Intento 1: Impresora térmica
                ticketPrinter.printTicket(comprobante, renglonesEfectivos)
            } catch (e: PrintingException) {
                // Intento 2 (Fallback): Generar PDF para impresión estándar de Android
                Log.w("PrintingManager", "Error de impresora térmica, generando PDF: ${e.message}")
                try {
                    val file = pdfGenerator.createPdfTicket(context, comprobante, renglonesEfectivos)
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

    // NUEVO: Forzar impresión como PDF (útil para pruebas del layout)
    suspend fun printAsPdf(
        context: Context,
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val pdfGenerator = PdfTicketGenerator()
                val file = pdfGenerator.createPdfTicket(context, comprobante, renglones)
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "${context.getString(R.string.app_name)} Documento"
                withContext(Dispatchers.Main) {
                    printManager.print(jobName, PdfPrintDocumentAdapter(context, file), null)
                }
            } catch (e: Exception) {
                Log.e("PrintingManager", "Error al generar o imprimir el PDF", e)
                withContext(Dispatchers.Main) {
                    NotificationManager.show("Error al crear el PDF del comprobante.", NotificationType.ERROR)
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

    suspend fun printCierreCaja(
        context: Context,
        filtros: ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaFiltros,
        resumen: ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaResumen
    ) {
        withContext(Dispatchers.IO) {
            try {
                val cierrePrinter = CierreCajaPrinter()
                cierrePrinter.print(filtros, resumen)
                // Además: generar y guardar PDF aunque la impresión térmica haya sido exitosa
                try {
                    val pdfGenerator = CierreCajaPdfGenerator()
                    val file = pdfGenerator.createPdfCierreCaja(context, filtros, resumen)
                    withContext(Dispatchers.Main) {
                        NotificationManager.show("Cierre de caja guardado en PDF: ${file.absolutePath}", NotificationType.SUCCESS)
                    }
                } catch (pdfOkError: Exception) {
                    Log.e("PrintingManager", "Error al generar el PDF tras impresión exitosa", pdfOkError)
                }
            } catch (e: Exception) {
                Log.e("PrintingManager", "Error al imprimir el cierre de caja", e)
                // Fallback: generar y guardar PDF automáticamente
                try {
                    val pdfGenerator = CierreCajaPdfGenerator()
                    val file = pdfGenerator.createPdfCierreCaja(context, filtros, resumen)
                    withContext(Dispatchers.Main) {
                        NotificationManager.show("Cierre de caja guardado en PDF: ${file.absolutePath}", NotificationType.SUCCESS)
                    }
                } catch (pdfError: Exception) {
                    Log.e("PrintingManager", "Error al generar el PDF del cierre de caja", pdfError)
                    withContext(Dispatchers.Main) {
                        NotificationManager.show("No se pudo imprimir ni guardar el cierre en PDF.", NotificationType.ERROR)
                    }
                }
            }
        }
    }
}