package ar.com.nexofiscal.nexofiscalposv2.network

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.*
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS) // Tiempo para establecer la conexión
        .readTimeout(120, TimeUnit.SECONDS)    // Tiempo de espera para leer datos
        .writeTimeout(120, TimeUnit.SECONDS)   // Tiempo de espera para enviar datos
        .build()

    @Volatile
    private var retrofit: Retrofit? = null
    @Volatile
    private var currentBaseUrl: String? = null

    private fun ensureRetrofit(): Retrofit {
        val target = SessionManager.getApiBaseUrl().ensureSlash()
        if (retrofit == null || currentBaseUrl != target) {
            Log.i(TAG, "(Re)creando Retrofit con baseUrl=$target")
            currentBaseUrl = target
            retrofit = Retrofit.Builder()
                .baseUrl(target)
                .client(okHttpClient)
                .build()
            api = retrofit!!.create(ApiService::class.java)
        }
        return retrofit!!
    }

    // api ya no es val inmutable
    @Volatile
    private var api: ApiService? = null

    fun refreshBaseUrl() { // Forzar recreación en próximo uso
        currentBaseUrl = null
    }

    private val gson = com.google.gson.Gson()

    private fun getApi(): ApiService {
        ensureRetrofit()
        return api!!
    }

    fun String.ensureSlash(): String = if (endsWith('/')) this else this + "/"

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
        val effectiveHeaders = headers ?: mutableMapOf()
        val service = getApi()

        var jsonBody: String? = null
        val call: Call<ResponseBody?> = when (method) {
            HttpMethod.GET -> service.requestGet(url, effectiveHeaders)!!
            HttpMethod.DELETE -> service.requestDelete(url, effectiveHeaders)!!
            HttpMethod.POST -> {
                jsonBody = buildRawJson(bodyObject)
                val postBody: RequestBody = jsonBody!!.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                service.requestPost(url, effectiveHeaders, postBody)!!
            }
            HttpMethod.PUT -> {
                jsonBody = buildRawJson(bodyObject)
                val putBody: RequestBody = jsonBody!!.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                service.requestPut(url, effectiveHeaders, putBody)!!
            }
        }

        // Sanitizar headers para logs
        val safeHeaders = effectiveHeaders.mapNotNull { (k, v) ->
            if (k == null) null else {
                val safeV = if (k.equals("Authorization", true) && !v.isNullOrBlank()) {
                    if (v.length > 15) v.substring(0, 10) + "..." + v.takeLast(5) else "***";
                } else v
                k to safeV
            }
        }.toMap()

        Log.d(TAG, buildString {
            append("[REQUEST] ${method.name} ${call.request().url}\n")
            append("Headers: $safeHeaders\n")
            if (jsonBody != null) append("Body: $jsonBody\n")
            append("BaseUrlActual=$currentBaseUrl")
        })

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(c: Call<ResponseBody?>, r: Response<ResponseBody?>) {
                val httpCode: Int = r.code()
                if (!r.isSuccessful) {
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
                    if (jsonElement.isJsonObject) {
                        val jsonObject = jsonElement.asJsonObject
                        val statusInJson = jsonObject.get("status")?.asInt
                        val statusMessageInJson = jsonObject.get("status_message")?.asString
                        if (statusInJson != null && statusInJson !in 200..299) {
                            val errorMessage = statusMessageInJson ?: "Error en la respuesta de la API."
                            Log.e(TAG, "Error lógico de la API (status JSON: $statusInJson): $errorMessage")
                            callback.onError(statusInJson, errorMessage)
                            return
                        }
                    }
                    val payload = gson.fromJson<T?>(json, responseType)
                    callback.onSuccess(httpCode, r.headers(), payload)
                } catch (e: java.io.IOException) {
                    Log.e(TAG, "Error de Parseo IO", e)
                    callback.onError(httpCode, "Error de Parseo IO: " + e.message)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    Log.e(TAG, "Error de Sintaxis JSON", e)
                    callback.onError(httpCode, "Error de Sintaxis JSON: " + e.message)
                } catch (e: Exception) {
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

    // Construye el JSON evitando doble serialización cuando ya viene como String o JSONObject
    private fun buildRawJson(bodyObject: Any?): String = when (bodyObject) {
        null -> "{}" // evita null body
        is String -> {
            // Si ya parece ser JSON (empieza con { o [) lo dejamos; si no, lo envolvemos en comillas
            val t = bodyObject.trim()
            if ((t.startsWith('{') && t.endsWith('}')) || (t.startsWith('[') && t.endsWith(']'))) t else com.google.gson.Gson().toJson(bodyObject)
        }
        is JSONObject -> bodyObject.toString()
        else -> com.google.gson.Gson().toJson(bodyObject)
    }

    private fun safeString(body: ResponseBody?): String? {
        return try {
            body?.string()
        } catch (e: java.io.IOException) {
            e.message
        }
    }
}