package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toCierreCajaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCaja
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object CierreCajaManager {
    private const val TAG = "CierreCajaManager"
    private const val ENDPOINT_CIERRES_CAJA = "/api/cierres_cajas"

    fun obtenerCierresCaja(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: CierreCajaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<CierreCaja?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<CierreCaja>(ENDPOINT_CIERRES_CAJA, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).cierreCajaDao()
                val entities = allItems.toCierreCajaEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} cierres de caja guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de cierres de caja: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    // Nuevo: imprimir cierre con fallback a PDF
    suspend fun imprimirCierre(
        context: Context,
        cierreId: Int,
        printFunc: (String) -> Boolean
    ): File? {
        return try {
            val texto = CierreCajaService.generarTextoImpresion(context, cierreId)
            val ok = try { printFunc(texto) } catch (_: Exception) { false }
            if (ok) null else CierreCajaService.generarInformeCierreEnPdf(context, cierreId).file
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al generar texto de impresión, generando PDF: ${e.message}")
            CierreCajaService.generarInformeCierreEnPdf(context, cierreId).file
        }
    }

    interface CierreCajaListCallback {
        fun onSuccess(cierresCaja: MutableList<CierreCaja?>?)
        fun onError(errorMessage: String?)
    }
}