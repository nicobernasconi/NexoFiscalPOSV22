package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toUnidadEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Unidad
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object UnidadManager {
    private const val TAG = "UnidadManager"
    private const val ENDPOINT_UNIDADES = "/api/unidades"

    fun obtenerUnidades(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: UnidadListCallback
    ) {
        val unidadListType = object :
            com.google.gson.reflect.TypeToken<MutableList<Unidad?>?>() {}.type

        ApiClient.request(
            HttpMethod.GET,
            ENDPOINT_UNIDADES,
            headers,
            null,
            unidadListType,
            object : ApiCallback<MutableList<Unidad?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    unidades: MutableList<Unidad?>?
                ) {
                    callback.onSuccess(unidades)

                    unidades?.let { listaUnidades ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val unidadDao = AppDatabase.getInstance(context.applicationContext).unidadDao()
                                val unidadEntities = listaUnidades.toUnidadEntityList()
                                unidadDao.upsertAll(unidadEntities)
                                Log.d(TAG, "${unidadEntities.size} unidades guardadas/actualizadas en la BD.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar unidades en la BD: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(
                        TAG,
                        "Error al obtener unidades. Código: $statusCode, Mensaje: $errorMessage"
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface UnidadListCallback {
        fun onSuccess(unidades: MutableList<Unidad?>?)
        fun onError(errorMessage: String?)
    }
}