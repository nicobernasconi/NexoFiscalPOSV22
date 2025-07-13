package ar.com.nexofiscal.nexofiscalposv2.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.*
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS) // Tiempo para establecer la conexión
        .readTimeout(120, TimeUnit.SECONDS)    // Tiempo de espera para leer datos
        .writeTimeout(120, TimeUnit.SECONDS)   // Tiempo de espera para enviar datos
        .build()


    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl("https://test.nexofiscaltest.com.ar/")
            .client(okHttpClient)
            .build()
    private val api: ApiService = retrofit.create(ApiService::class.java)
    private val gson = com.google.gson.Gson()

    /**
     * @param method       GET, POST, PUT o DELETE
     * @param url          URL completa (puede incluir query params)
     * @param headers      Map<String,String> de cabeceras (o null para none)
     * @param bodyObject   Objeto Java que se convertirá a JSON (solo POST/PUT; null para GET/DELETE)
     * @param responseType Type de respuesta (por ejemplo MyModel.class o new TypeToken<List<MyModel>>(){}.getType() )
     * @param callback     Callback con status, headers y payload parseado
     */
    fun <T> request(
        method: HttpMethod,
        url: String?,
        headers: MutableMap<String?, String?>?,
        bodyObject: Any?,
        responseType: java.lang.reflect.Type,
        callback: ApiCallback<T?>
    ) {
        var effectiveHeaders = headers ?: mutableMapOf()

        val call: Call<ResponseBody?> = when (method) {
            HttpMethod.GET -> api.requestGet(url, effectiveHeaders)!!
            HttpMethod.DELETE -> api.requestDelete(url, effectiveHeaders)!!
            HttpMethod.POST -> {
                val postBody: RequestBody = gson.toJson(bodyObject)
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                api.requestPost(url, effectiveHeaders, postBody)!!
            }
            HttpMethod.PUT -> {
                val putBody: RequestBody = gson.toJson(bodyObject)
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                api.requestPut(url, effectiveHeaders, putBody)!!
            }
        }

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(c: Call<ResponseBody?>, r: Response<ResponseBody?>) {
                val httpCode: Int = r.code()
                if (!r.isSuccessful()) {
                    val errorMsg = r.errorBody()?.let { safeString(it) } ?: r.message()
                    Log.e(TAG, "Respuesta de error HTTP (código: $httpCode) para la URL: ${c.request().url}. Body: $errorMsg")
                    callback.onError(httpCode, errorMsg)
                    return
                }

                try {
                    val json: String? = r.body()?.string()
                    Log.d(TAG, "Respuesta del servidor (HTTP $httpCode) para la URL: ${c.request().url}")
                    Log.d(TAG, "Parametros de la petición: ${c.request().url.queryParameterNames.joinToString(", ")}")
                    Log.d(TAG, "Body de la Respuesta: $json")

                    if (json.isNullOrBlank()) {
                        callback.onSuccess(httpCode, r.headers(), null)
                        return
                    }

                    val jsonElement = com.google.gson.JsonParser.parseString(json)

                    // Verificamos si la respuesta es un objeto JSON para chequear el status.
                    if (jsonElement.isJsonObject) {
                        val jsonObject = jsonElement.asJsonObject
                        val statusInJson = jsonObject.get("status")?.asInt
                        val statusMessageInJson = jsonObject.get("status_message")?.asString

                        if (statusInJson != null && statusInJson !in 200..299) {
                            val errorMessage = statusMessageInJson ?: "Error en la respuesta de la API."
                            Log.e(TAG, "Error lógico de la API (status JSON: $statusInJson): $errorMessage")
                            callback.onError(statusInJson, errorMessage)
                            return // Finalizamos, es un error lógico.
                        }
                    }
                    // ---- FIN DE LA NUEVA LÓGICA ----

                    // 3. Si no hay error lógico, procedemos como antes
                    val payload = gson.fromJson<T?>(json, responseType)
                    callback.onSuccess(httpCode, r.headers(), payload)

                } catch (e: java.io.IOException) {
                    Log.e(TAG, "Error de Parseo IO", e)
                    callback.onError(httpCode, "Error de Parseo IO: " + e.message)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    Log.e(TAG, "Error de Sintaxis JSON", e)
                    callback.onError(httpCode, "Error de Sintaxis JSON: " + e.message)
                } catch (e: Exception) {
                    // Captura cualquier otro error durante el parseo del JSON, como un campo inesperado
                    Log.e(TAG, "Error procesando la respuesta JSON", e)
                    callback.onError(httpCode, "Error inesperado al procesar la respuesta: " + e.message)
                }
            }

            override fun onFailure(c: Call<ResponseBody?>, t: Throwable) {
                Log.e(TAG, "Fallo en la petición a ${c.request().url}", t)
                callback.onError(-1, t.message)
            }
        })
    }

    private fun safeString(body: ResponseBody?): String? {
        return try {
            body?.string()
        } catch (e: java.io.IOException) {
            e.message
        }
    }
}