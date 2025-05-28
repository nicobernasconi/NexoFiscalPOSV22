package ar.com.nexofiscal.nexofiscalposv2.network

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.*

object ApiClient {
    private const val TAG = "ApiClient"
    private val retrofit: Retrofit =
        Retrofit.Builder() // Retrofit exige un baseUrl "dummy", luego siempre sobreescribimos con @Url
            .baseUrl("https://test.nexofiscaltest.com.ar/")
            .build()
    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val gson = com.google.gson.Gson()

    /**
     * @param method       GET, POST, PUT o DELETE
     * @param url          URL completa (puede incluir query params)
     * @param headers      Map<String></String>,String> de cabeceras (o null para none)
     * @param bodyObject   Objeto Java que se convertirá a JSON (solo POST/PUT; null para GET/DELETE)
     * @param responseType Type de respuesta (por ejemplo MyModel.class o new TypeToken<List></List><MyModel>>(){}.getType() )
     * @param callback     Callback con status, headers y payload parseado
    </MyModel> */
    fun <T> request(
        method: ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod,
        url: kotlin.String?,
        headers: kotlin.collections.MutableMap<kotlin.String?, kotlin.String?>?,
        bodyObject: kotlin.Any?,
        responseType: java.lang.reflect.Type,
        callback: ApiCallback<T?>
    ) {
        var headers = headers
        if (headers == null) headers =
            kotlin.collections.mutableMapOf<kotlin.String?, kotlin.String?>()

        val call: Call<ResponseBody?>
        when (method) {
            ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod.GET -> call =
                ApiClient.api.requestGet(url, headers)!!

            ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod.DELETE -> call =
                ApiClient.api.requestDelete(url, headers)!!

            ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod.POST -> {val postBody: RequestBody? = ApiClient.gson.toJson(bodyObject)
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                call = ApiClient.api.requestPost(url, headers, postBody)!!
            }

            ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod.PUT -> {
                val putBody: RequestBody? = ApiClient.gson.toJson(bodyObject)
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                call = ApiClient.api.requestPut(url, headers, putBody)!!
            }

            else -> {
                callback.onError(-1, "Método HTTP no soportado")
                return
            }
        }

        call.enqueue(object : Callback<ResponseBody?> {
            public override fun onResponse(c: Call<ResponseBody?>?, r: Response<ResponseBody?>) {
                val code: kotlin.Int = r.code()
                if (!r.isSuccessful() || r.body() == null) {
                    val msg =
                        (if (r.errorBody() != null) ApiClient.safeString(r.errorBody()) else r.message())
                    callback.onError(code, msg)
                    return
                }
                try {
                    val json: kotlin.String? = r.body()?.string()
                    val payload = ApiClient.gson.fromJson<T?>(json, responseType)
                    callback.onSuccess(code, r.headers(), payload)
                } catch (e: java.io.IOException) {
                    android.util.Log.e(ApiClient.TAG, "Parse error", e)
                    callback.onError(code, "Parse error: " + e.message)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    android.util.Log.e(ApiClient.TAG, "Parse error", e)
                    callback.onError(code, "Parse error: " + e.message)
                }
            }

            public override fun onFailure(c: Call<ResponseBody?>?, t: kotlin.Throwable) {
                callback.onError(-1, t.message)
            }
        })
    }

    private fun safeString(body: ResponseBody?): kotlin.String? {
        try {
            return body?.string()
        } catch (e: java.io.IOException) {
            return e.message
        }
    }
}
