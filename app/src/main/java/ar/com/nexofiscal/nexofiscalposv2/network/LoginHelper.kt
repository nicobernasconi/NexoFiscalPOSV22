package ar.com.nexofiscal.nexofiscalposv2.network

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/* --------- MODELO que Retrofit serializará a JSON ---------- */
class LoginRequest(var usuario: String?, var password: String?)

/* ------------------ HELPER DE LOGIN ------------------------ */
object LoginHelper {
    fun login(
        context: Context,
        baseUrl: String?,
        usuario: String?,
        password: String?,
        callback: LoginCallback
    ) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val service: LoginService = retrofit.create(LoginService::class.java)
        val request = LoginRequest(usuario, password)
        val call: Call<String?> = service.login(request)

        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>?, resp: Response<String?>) {
                if (resp.isSuccessful() && resp.body() != null) {
                    try {
                        val json: JSONObject = JSONObject(resp.body())

                        if ("200" == json.optString("status")) {
                            val prefs: SharedPreferences =
                                context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
                            val ed: SharedPreferences.Editor = prefs.edit()

                            // Guardar datos del nivel raíz
                            ed.putString("token", json.optString("token"))
                            ed.putInt("usuario_id", json.optInt("usuario_id"))

                            // Guardar datos del objeto "usuario"
                            val usuarioObj = json.getJSONObject("usuario")
                            ed.putInt("usuario_id", usuarioObj.optInt("usuario_id"))
                            ed.putString("nombre_usuario", usuarioObj.optString("nombre_usuario"))
                            ed.putString("nombre_completo", usuarioObj.optString("nombre_completo"))
                            ed.putInt("rol_id", usuarioObj.optInt("rol_id"))
                            ed.putInt("venta_rapida", usuarioObj.optInt("venta_rapida"))
                            ed.putInt("imprimir", usuarioObj.optInt("imprimir"))
                            ed.putInt("tipo_comprobante_imprimir", usuarioObj.optInt("tipo_comprobante_imprimir"))
                            ed.putString("razon_social", usuarioObj.optString("razon_social"))
                            ed.putString("iibb", usuarioObj.optString("iibb"))
                            ed.putString("fecha_inicio_actividades", usuarioObj.optString("fecha_inicio_actividades"))
                            ed.putString("cert", usuarioObj.optString("cert"))
                            ed.putString("key", usuarioObj.optString("key"))
                            ed.putString("gestor_logo", usuarioObj.optString("gestor_logo"))
                            ed.putString("permisos", usuarioObj.optString("permisos"))

                            // --- INICIO DE CAMPOS NUEVOS/ACTUALIZADOS ---
                            ed.putInt("emite_comprobante_caja", usuarioObj.optInt("emite_comprobante_caja"))
                            ed.putInt("long", usuarioObj.optInt("long"))
                            // --- FIN DE CAMPOS NUEVOS/ACTUALIZADOS ---


                            // Guardar datos del objeto anidado "empresa"
                            val empresaObj = usuarioObj.getJSONObject("empresa")
                            ed.putInt("empresa_id", empresaObj.optInt("id"))
                            ed.putString("empresa_nombre", empresaObj.optString("nombre"))
                            ed.putString("empresa_logo", empresaObj.optString("logo"))
                            ed.putString("empresa_direccion", empresaObj.optString("direccion"))
                            ed.putString("empresa_telefono", empresaObj.optString("telefono"))
                            ed.putString("empresa_cuit", empresaObj.optString("cuit"))
                            ed.putInt("empresa_tipo_iva", empresaObj.optInt("tipo_iva"))
                            ed.putString("empresa_responsable", empresaObj.optString("responsable"))
                            ed.putString("empresa_email", empresaObj.optString("email"))
                            ed.putString("empresa_razon_social", empresaObj.optString("razon_social"))
                            ed.putString("empresa_iibb", empresaObj.optString("iibb"))
                            ed.putString("empresa_fecha_inicio_actividades", empresaObj.optString("fecha_inicio_actividades"))
                            ed.putString("empresa_cert", empresaObj.optString("cert"))
                            ed.putString("empresa_key", empresaObj.optString("key"))

                            // --- INICIO DE CAMPOS NUEVOS/ACTUALIZADOS ---
                            ed.putString("codigos_barras_id_long", empresaObj.optString("codigos_barras_id_long"))
                            ed.putInt("codigos_barras_long", empresaObj.optInt("codigos_barras_long"))
                            ed.putInt("codigos_barras_inicio", empresaObj.optInt("codigos_barras_inicio"))
                            ed.putInt("codigos_barras_payload_int", empresaObj.optInt("codigos_barras_payload_int"))
                            ed.putString("codigos_barras_payload_type", empresaObj.optString("codigos_barras_payload_type"))
                            // --- FIN DE CAMPOS NUEVOS/ACTUALIZADOS ---


                            // Guardar datos del objeto anidado "sucursal"
                            val sucursalObj = usuarioObj.getJSONObject("sucursal")
                            ed.putInt("sucursal_id", sucursalObj.optInt("id"))
                            ed.putString("sucursal_nombre", sucursalObj.optString("nombre"))
                            ed.putString("sucursal_direccion", sucursalObj.optString("direccion"))

                            // Guardar datos del objeto anidado "vendedor"
                            val vendedorObj = usuarioObj.getJSONObject("vendedor")
                            ed.putInt("vendedor_id", vendedorObj.optInt("id"))
                            ed.putString("vendedor_nombre", vendedorObj.optString("nombre"))

                            // Guardar datos del objeto anidado "punto_venta"
                            val puntoVentaObj = usuarioObj.getJSONObject("punto_venta")
                            ed.putInt("punto_venta_id", puntoVentaObj.optInt("id"))
                            ed.putInt("punto_venta_numero", puntoVentaObj.optInt("numero"))
                            ed.putString("punto_venta_descripcion", puntoVentaObj.optString("descripcion"))

                            ed.putLong("login_time", System.currentTimeMillis())
                            ed.apply()

                            callback.onSuccess(json)
                        } else {
                            callback.onError(json.optString("message", "Login fallido"), false)
                        }
                    } catch (e: Exception) {
                        callback.onError("Error procesando la respuesta: " + e.message, false)
                    }
                } else {
                    callback.onError("Error HTTP: " + resp.code(), false)
                }
            }

            override fun onFailure(call: Call<String?>?, t: Throwable) {
                callback.onError("Error de red: " + t.message, true)
            }
        })
    }

    interface LoginService {
        @POST("api/login")
        fun login(@Body request: LoginRequest?): Call<String?>
    }

    interface LoginCallback {
        fun onSuccess(response: JSONObject?)
        fun onError(error: String?, isNetworkError: Boolean)
    }
}