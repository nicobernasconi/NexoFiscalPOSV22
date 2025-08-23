package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

object SessionManager {

    private const val PREFS_NAME = "nexofiscal"
    private lateinit var prefs: SharedPreferences

    // Valor por defecto de la API
    private const val DEFAULT_API_BASE_URL = "https://test.nexofiscaltest.com.ar/"
    // Valor por defecto del PIN de configuración avanzada
    private const val DEFAULT_ADVANCED_PIN = "2468"
    // Defaults para tiempos (en minutos)
    private const val DEFAULT_TIEMPO_DESCARGA_MIN = 15
    private const val DEFAULT_TIEMPO_SUBIDA_MIN = 15
    private const val DEFAULT_MAX_BACKUPS = 30

    // --- DATOS DE USUARIO Y SESIÓN ---
    val token: String? get() = prefs.getString("token", null)
    val usuarioId: Int get() = prefs.getInt("usuario_id", -1)
    val nombreCompleto: String? get() = prefs.getString("nombre_completo", null)
    val rolId: Int get() = prefs.getInt("rol_id", -1)

    // --- CONFIGURACIONES DE OPERACIÓN ---
    val usaVentaRapida: Boolean get() = prefs.getInt("venta_rapida", 0) == 1
    val puedeImprimir: Boolean get() = prefs.getInt("imprimir", 0) == 1
    val emiteComprobanteCaja: Boolean get() = prefs.getInt("emite_comprobante_caja", 0) == 1
    val tipoComprobanteImprimir: Int get() = prefs.getInt("tipo_comprobante_imprimir", 1)

    // --- DATOS DE LA EMPRESA ---
    val empresaId: Int get() = prefs.getInt("empresa_id", -1)
    val empresaNombre: String? get() = prefs.getString("empresa_nombre", null)
    val empresaLogoBase64: String? get() = prefs.getString("empresa_logo", null)
    val empresaDireccion: String? get() = prefs.getString("empresa_direccion", null)
    val empresaCuit: String? get() = prefs.getString("empresa_cuit", null)
    val empresaRazonSocial: String? get() = prefs.getString("empresa_razon_social", null)
    val empresaIIBB: String? get() = prefs.getString("empresa_iibb", null)
    val empresaInicioActividades: String? get() = prefs.getString("empresa_fecha_inicio_actividades", null)
    val empresaTipoIva: Int get() = prefs.getInt("empresa_tipo_iva", -1)

    // --- DATOS DE SUCURSAL Y PUNTO DE VENTA ---
    val sucursalId: Int get() = prefs.getInt("sucursal_id", -1)
    val sucursalNombre: String? get() = prefs.getString("sucursal_nombre", null)
    val puntoVentaNumero: Int get() = prefs.getInt("punto_venta_numero", -1)

    // --- DATOS DE CONFIGURACIÓN DE CÓDIGO DE BARRAS ---
    val codigosBarrasIdLong: String? get() = prefs.getString("codigos_barras_id_long", null)
    val codigosBarrasLong: Int get() = prefs.getInt("codigos_barras_long", 13)
    val codigosBarrasInicio: Int get() = prefs.getInt("codigos_barras_inicio", 0)
    val codigosBarrasPayloadInt: Int get() = prefs.getInt("codigos_barras_payload_int", 0)
    val codigosBarrasPayloadType: String? get() = prefs.getString("codigos_barras_payload_type", null)
    val certificadoAfip: String? get() = prefs.getString("empresa_cert", null)
    val claveAfip: String? get() = prefs.getString("empresa_key", null)

    // --- CONFIGURACIONES AVANZADAS / OCULTAS ---
    fun getApiBaseUrl(): String = prefs.getString("api_base_url", DEFAULT_API_BASE_URL) ?: DEFAULT_API_BASE_URL
    fun setApiBaseUrl(url: String) { prefs.edit().putString("api_base_url", url).apply() }
    fun resetApiBaseUrl() { setApiBaseUrl(DEFAULT_API_BASE_URL) }

    fun getAdvancedConfigPin(): String = prefs.getString("advanced_config_pin", DEFAULT_ADVANCED_PIN) ?: DEFAULT_ADVANCED_PIN
    fun setAdvancedConfigPin(pin: String) { prefs.edit().putString("advanced_config_pin", pin).apply() }
    fun validateAdvancedPin(input: String): Boolean = input == getAdvancedConfigPin()
    // Nuevo: reset explícito del PIN avanzado
    fun resetAdvancedConfigPin() { setAdvancedConfigPin(DEFAULT_ADVANCED_PIN) }

    // Nuevo: acceso directo al JSON crudo de permisos para edición avanzada
    fun getPermissionsJson(): String = prefs.getString("permisos", "{}") ?: "{}"

    // Nuevos getters/setters para tiempos de sincronización (modo avanzado)
    fun getTiempoDescargaMin(): Int = prefs.getInt("tiempo_descarga_min", DEFAULT_TIEMPO_DESCARGA_MIN)
    fun setTiempoDescargaMin(value: Int) { prefs.edit().putInt("tiempo_descarga_min", value).apply() }
    fun getTiempoSubidaMin(): Int = prefs.getInt("tiempo_subida_min", DEFAULT_TIEMPO_SUBIDA_MIN)
    fun setTiempoSubidaMin(value: Int) { prefs.edit().putInt("tiempo_subida_min", value).apply() }
    fun getMaxBackups(): Int = prefs.getInt("max_backups", DEFAULT_MAX_BACKUPS)
    fun setMaxBackups(value: Int) { prefs.edit().putInt("max_backups", value).apply() }

    @Volatile
    private var permissionsMap: Map<String, List<String>> = emptyMap()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // cargar permisos desde prefs
        permissionsMap = parsePermissions(prefs.getString("permisos", "{}"))
    }

    private fun parsePermissions(jsonString: String?): Map<String, List<String>> {
        if (jsonString.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, List<String>>()
        try {
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

    fun hasPermission(entity: String, action: String): Boolean {
        return permissionsMap[entity]?.contains(action) ?: false
    }

    /**
     * Permite actualizar/establecer los permisos en caliente.
     * Guarda el JSON en SharedPreferences y actualiza el mapa en memoria.
     */
    fun setPermissionsJson(json: String?) {
        prefs.edit().putString("permisos", json ?: "{}").apply()
        permissionsMap = parsePermissions(json)
    }

    /** Limpia permisos actuales (vacía el mapa en memoria y prefs). */
    fun clearPermissions() {
        prefs.edit().remove("permisos").apply()
        permissionsMap = emptyMap()
    }
}