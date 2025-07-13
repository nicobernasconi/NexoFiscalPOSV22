package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTipoDocumentoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.TipoDocumento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TipoDocumentoManager {
    private const val TAG = "TipoDocumentoManager"
    private const val ENDPOINT_TIPOS_DOCUMENTO = "/api/tipos_documento"

    fun obtenerTiposDocumento(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TipoDocumentoListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<TipoDocumento?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<TipoDocumento>(ENDPOINT_TIPOS_DOCUMENTO, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).tipoDocumentoDao()
                val entities = allItems.toTipoDocumentoEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} tipos de documento guardados/actualizados.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de tipos de documento: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface TipoDocumentoListCallback {
        fun onSuccess(tiposDocumento: MutableList<TipoDocumento?>?)
        fun onError(errorMessage: String?)
    }
}