package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTipoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Tipo
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object TipoManager {
    private const val TAG = "TipoManager"
    private const val ENDPOINT_TIPOS = "/api/tipos" // Este endpoint es genérico, podrías necesitar uno más específico

    fun obtenerTipos(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TipoListCallback
    ) {
        val tipoListType = object :
            com.google.gson.reflect.TypeToken<MutableList<Tipo?>?>() {}.type

        ApiClient.request(
            HttpMethod.GET,
            ENDPOINT_TIPOS,
            headers,
            null,
            tipoListType,
            object : ApiCallback<MutableList<Tipo?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    tipos: MutableList<Tipo?>?
                ) {
                    callback.onSuccess(tipos)

                    tipos?.let { listaTipos ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val tipoDao = AppDatabase.getInstance(context.applicationContext).tipoDao()
                                val tipoEntities = listaTipos.toTipoEntityList()
                                tipoDao.upsertAll(tipoEntities)
                                Log.d(TAG, "${tipoEntities.size} tipos guardados/actualizados en la BD.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar tipos en la BD: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(
                        TAG,
                        "Error al obtener tipos. Código: $statusCode, Mensaje: $errorMessage"
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface TipoListCallback {
        fun onSuccess(tipos: MutableList<Tipo?>?)
        fun onError(errorMessage: String?)
    }
}