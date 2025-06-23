package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTipoFormaPagoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TipoFormaPagoManager {
    private const val TAG = "TipoFormaPagoManager"
    private const val ENDPOINT_TIPOS_FORMA_PAGO = "/api/tipos_forma_pago"

    fun obtenerTiposFormaPago(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TipoFormaPagoListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<TipoFormaPago?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<TipoFormaPago>(ENDPOINT_TIPOS_FORMA_PAGO, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).tipoFormaPagoDao()
                val entities = allItems.toTipoFormaPagoEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} tipos de forma de pago guardados.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de tipos de forma de pago: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface TipoFormaPagoListCallback {
        fun onSuccess(tiposFormaPago: MutableList<TipoFormaPago?>?)
        fun onError(errorMessage: String?)
    }
}