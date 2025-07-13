package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTipoComprobanteEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TipoComprobanteManager {
    private const val TAG = "TipoComprobanteManager"
    private const val ENDPOINT_TIPOS_COMPROBANTE = "/api/tipos_comprobante"

    fun obtenerTiposComprobante(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TipoComprobanteListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<TipoComprobante?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<TipoComprobante>(ENDPOINT_TIPOS_COMPROBANTE, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).tipoComprobanteDao()
                val entities = allItems.toTipoComprobanteEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} tipos de comprobante guardados/actualizados.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de tipos de comprobante: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface TipoComprobanteListCallback {
        fun onSuccess(tiposComprobante: MutableList<TipoComprobante?>?)
        fun onError(errorMessage: String?)
    }
}