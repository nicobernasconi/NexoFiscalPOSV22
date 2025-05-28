package ar.com.nexofiscal.nexofiscalposv2.network

import okhttp3.Headers

interface ApiCallback<T> {
    /** Código HTTP y cabeceras  */
    fun onSuccess(statusCode: Int, headers: Headers?, payload: T?)
    fun onError(statusCode: Int, errorMessage: String?)
}
