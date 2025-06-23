package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import okhttp3.Headers

object RenglonComprobanteManager {
    private const val TAG = "RenglonComprobanteMgr"
    private const val ENDPOINT_BASE_RENGLONES = "/api/renglones_comprobantes_masivo"
    private const val PAGE_SIZE = 1000
    private const val TARGET_COUNT = 1000

    fun obtenerRenglonesMasivamente(
        context: Context,
        comprobanteIds: String,
        headers: MutableMap<String?, String?>?,
        callback: RenglonComprobanteListCallback
    ) {
        // CORRECCIÓN: El tipo de dato se define como una lista simple de RenglonComprobante,
        // para que coincida con la estructura JSON que la API devuelve.
        val renglonListType = object :
            com.google.gson.reflect.TypeToken<MutableList<RenglonComprobante?>?>() {}.type

        val url = "$ENDPOINT_BASE_RENGLONES?comprobante_id=$comprobanteIds&size=$PAGE_SIZE"

        ApiClient.request(
            HttpMethod.GET,
            url,
            headers,
            null,
            renglonListType,
            object : ApiCallback<MutableList<RenglonComprobante?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    renglones: MutableList<RenglonComprobante?>?
                ) {
                    // El resultado ya es una lista simple, se pasa directamente.
                    callback.onSuccess(renglones)
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(TAG, "Error al obtener renglones. Código: $statusCode, Mensaje: $errorMessage")
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface RenglonComprobanteListCallback {
        fun onSuccess(renglones: MutableList<RenglonComprobante?>?)
        fun onError(errorMessage: String?)
    }
}