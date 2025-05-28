package ar.com.nexofiscal.nexofiscalposv2.managers

import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod

object ClienteManager {
    private const val TAG = "ClienteManager"
    private const val ENDPOINT_CLIENTES =
        "/api/clientes" // Reemplaza con la ruta correcta de tu API

    fun obtenerClientes(
        headers: kotlin.collections.MutableMap<kotlin.String?, kotlin.String?>?,
        callback: ClienteListCallback
    ) {
        val clienteListType = object :
            com.google.gson.reflect.TypeToken<kotlin.collections.MutableList<Cliente?>?>() {}.getType()

        ApiClient.request(
            HttpMethod.GET,
            ClienteManager.ENDPOINT_CLIENTES,
            headers,
            null,
            clienteListType,
            object : ApiCallback<kotlin.collections.MutableList<Cliente?>?> {
                public override fun onSuccess(
                    statusCode: kotlin.Int,
                    headers: okhttp3.Headers?,
                    clientes: kotlin.collections.MutableList<Cliente?>?
                ) {
                    callback.onSuccess(clientes)
                }

                public override fun onError(statusCode: kotlin.Int, errorMessage: kotlin.String?) {
                    android.util.Log.e(
                        ClienteManager.TAG,
                        "Error al obtener clientes. Código: " + statusCode + ", Mensaje: " + errorMessage
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    } // Puedes agregar otros métodos para crear, actualizar o eliminar clientes si tu API lo permite.

    interface ClienteListCallback {
        fun onSuccess(clientes: kotlin.collections.MutableList<Cliente?>?)
        fun onError(errorMessage: kotlin.String?)
    }
}