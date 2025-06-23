package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * Gestor singleton para manejar los datos de la sesión del usuario.
 * Proporciona un único punto de acceso a la configuración y datos del usuario
 * almacenados en SharedPreferences.
 */
object SessionManager {

    private const val PREFS_NAME = "nexofiscal"
    private lateinit var prefs: SharedPreferences

    // --- PROPIEDADES DE ACCESO INDIVIDUAL ---

    // Datos principales
    val token: String? get() = prefs.getString("token", null)
    val usuarioId: Int get() = prefs.getInt("usuario_id", -1)
    val nombreCompleto: String? get() = prefs.getString("nombre_completo", null)
    val rolId: Int get() = prefs.getInt("rol_id", -1)

    // Datos de la Empresa
    val empresaId: Int get() = prefs.getInt("empresa_id", -1)
    val empresaNombre: String? get() = prefs.getString("empresa_nombre", null)
    val empresaLogoBase64: String? get() = prefs.getString("empresa_logo", null)
    val empresaDireccion: String? get() = prefs.getString("empresa_direccion", null)
    val empresaTelefono: String? get() = prefs.getString("empresa_telefono", null)
    val empresaCuit: String? get() = prefs.getString("empresa_cuit", null)
    val empresaRazonSocial: String? get() = prefs.getString("empresa_razon_social", null)
    val empresaIIBB: String? get() = prefs.getString("empresa_iibb", null)
    val empresaInicioActividades: String? get() = prefs.getString("empresa_fecha_inicio_actividades", null)

    // Datos de la Sucursal y Punto de Venta
    val sucursalId: Int get() = prefs.getInt("sucursal_id", -1)
    val sucursalNombre: String? get() = prefs.getString("sucursal_nombre", null)
    val puntoVentaNumero: Int get() = prefs.getInt("punto_venta_numero", -1)

    // --- CAMBIO: Propiedades booleanas para un acceso más limpio ---
    val usaVentaRapida: Boolean get() = prefs.getInt("venta_rapida", 0) == 1
    val puedeImprimir: Boolean get() = prefs.getInt("imprimir", 0) == 1
    val emiteComprobanteCaja: Boolean get() = prefs.getInt("emite_comprobante_caja", 0) == 1
    val tipoComprobanteImprimir: Int get() = prefs.getInt("tipo_comprobante_imprimir", 1)

    // --- CAMBIO: Propiedad para gestionar los permisos ---
    private val permissionsMap: Map<String, List<String>> by lazy {
        parsePermissions(prefs.getString("permisos", "{}"))
    }

    /**
     * Inicializa el SessionManager con el contexto de la aplicación.
     * Debe llamarse una vez al iniciar la app, por ejemplo, en LoginActivity.
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda toda la información de la sesión desde la respuesta JSON del login.
     */
    fun saveSession(loginResponse: JSONObject) {
        val editor = prefs.edit()

        // Nivel raíz
        editor.putString("token", loginResponse.optString("token"))
        editor.putInt("usuario_id", loginResponse.optInt("usuario_id"))

        // Objeto 'usuario'
        val usuarioObj = loginResponse.getJSONObject("usuario")
        editor.putString("nombre_usuario", usuarioObj.optString("nombre_usuario"))
        editor.putString("nombre_completo", usuarioObj.optString("nombre_completo"))
        editor.putInt("rol_id", usuarioObj.optInt("rol_id"))
        editor.putInt("venta_rapida", usuarioObj.optInt("venta_rapida"))
        editor.putInt("imprimir", usuarioObj.optInt("imprimir"))
        editor.putInt("tipo_comprobante_imprimir", usuarioObj.optInt("tipo_comprobante_imprimir"))
        editor.putInt("emite_comprobante_caja", usuarioObj.optInt("emite_comprobante_caja"))
        editor.putString("razon_social", usuarioObj.optString("razon_social"))
        editor.putString("iibb", usuarioObj.optString("iibb"))
        editor.putString("fecha_inicio_actividades", usuarioObj.optString("fecha_inicio_actividades"))
        editor.putString("cert", usuarioObj.optString("cert"))
        editor.putString("key", usuarioObj.optString("key"))
        editor.putString("gestor_logo", usuarioObj.optString("gestor_logo"))
        editor.putString("permisos", usuarioObj.optString("permisos"))

        // Objeto 'empresa' (anidado)
        val empresaObj = usuarioObj.getJSONObject("empresa")
        editor.putInt("empresa_id", empresaObj.optInt("id"))
        editor.putString("empresa_nombre", empresaObj.optString("nombre"))
        editor.putString("empresa_logo", empresaObj.optString("logo"))
        editor.putString("empresa_direccion", empresaObj.optString("direccion"))
        editor.putString("empresa_telefono", empresaObj.optString("telefono"))
        editor.putString("empresa_cuit", empresaObj.optString("cuit"))
        editor.putInt("empresa_tipo_iva", empresaObj.optInt("tipo_iva"))
        editor.putString("empresa_responsable", empresaObj.optString("responsable"))
        editor.putString("empresa_email", empresaObj.optString("email"))
        editor.putString("empresa_razon_social", empresaObj.optString("razon_social"))
        editor.putString("empresa_iibb", empresaObj.optString("iibb"))
        editor.putString("empresa_fecha_inicio_actividades", empresaObj.optString("fecha_inicio_actividades"))
        editor.putString("empresa_cert", empresaObj.optString("cert"))
        editor.putString("empresa_key", empresaObj.optString("key"))

        // Objeto 'sucursal' (anidado)
        val sucursalObj = usuarioObj.getJSONObject("sucursal")
        editor.putInt("sucursal_id", sucursalObj.optInt("id"))
        editor.putString("sucursal_nombre", sucursalObj.optString("nombre"))
        editor.putString("sucursal_direccion", sucursalObj.optString("direccion"))

        // Objeto 'vendedor' (anidado)
        val vendedorObj = usuarioObj.getJSONObject("vendedor")
        editor.putInt("vendedor_id", vendedorObj.optInt("id"))
        editor.putString("vendedor_nombre", vendedorObj.optString("nombre"))

        // Objeto 'punto_venta' (anidado)
        val puntoVentaObj = usuarioObj.getJSONObject("punto_venta")
        editor.putInt("punto_venta_id", puntoVentaObj.optInt("id"))
        editor.putInt("punto_venta_numero", puntoVentaObj.optInt("numero"))
        editor.putString("punto_venta_descripcion", puntoVentaObj.optString("descripcion"))

        editor.putLong("login_time", System.currentTimeMillis())
        editor.apply()
    }

    /**
     * Parsea el string de permisos JSON a un mapa para una fácil consulta.
     */
    private fun parsePermissions(jsonString: String?): Map<String, List<String>> {
        if (jsonString.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, List<String>>()
        try {
            // El string de permisos es una serie de objetos JSON concatenados, no un array JSON válido.
            // Lo solucionamos envolviéndolo en corchetes para que sea un array JSON.
            val validJsonArrayString = "[$jsonString]"
            val jsonArray = org.json.JSONArray(validJsonArrayString)
            for (i in 0 until jsonArray.length()) {
                val permissionObj = jsonArray.getJSONObject(i)
                permissionObj.keys().forEach { key ->
                    val permissionsArray = permissionObj.getJSONArray(key)
                    val permissionsList = mutableListOf<String>()
                    for (j in 0 until permissionsArray.length()) {
                        permissionsList.add(permissionsArray.getString(j))
                    }
                    map[key] = permissionsList
                }
            }
        } catch (e: JSONException) {
            Log.e("SessionManager", "Error al parsear los permisos JSON: $jsonString", e)
        }
        return map
    }

    /**
     * Verifica si el usuario actual tiene un permiso específico para una entidad.
     * @param entity El nombre de la entidad (ej. "productos").
     * @param action El permiso a verificar (ej. "crear", "eliminar").
     * @return `true` si el usuario tiene el permiso, `false` en caso contrario.
     */
    fun hasPermission(entity: String, action: String): Boolean {
        return permissionsMap[entity]?.contains(action) ?: false
    }
}