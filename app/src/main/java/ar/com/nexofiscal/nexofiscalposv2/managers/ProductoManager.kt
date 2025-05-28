package ar.com.nexofiscal.nexofiscalposv2.managers

import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod

object ProductoManager {
    private const val TAG = "ProductoManager"
    private const val ENDPOINT_PRODUCTO = "/api/productos"

    fun obtenerProductos(
        headers: kotlin.collections.MutableMap<kotlin.String?, kotlin.String?>?,
        body: kotlin.String?,
        callback: ProductoListCallback
    ) {
        val productoListType = object :
            com.google.gson.reflect.TypeToken<kotlin.collections.MutableList<Producto?>?>() {}.getType()

        ApiClient.request(
            HttpMethod.GET,
            ProductoManager.ENDPOINT_PRODUCTO,
            headers,
            body,
            productoListType,
            object : ApiCallback<kotlin.collections.MutableList<Producto?>?> {
                public override fun onSuccess(
                    statusCode: kotlin.Int,
                    headers: okhttp3.Headers?,
                    productos: kotlin.collections.MutableList<Producto?>?
                ) {
                    callback.onSuccess(productos)
                }

                public override fun onError(statusCode: kotlin.Int, errorMessage: kotlin.String?) {
                    android.util.Log.e(
                        ProductoManager.TAG,
                        "Error al obtener productos. Código: " + statusCode + ", Mensaje: " + errorMessage
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }


    interface ProductoListCallback {
        fun onSuccess(productos: kotlin.collections.MutableList<Producto?>?)
        fun onError(errorMessage: kotlin.String?)
    }
}