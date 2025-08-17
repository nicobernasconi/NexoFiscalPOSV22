package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Notificacion {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("mensaje")
    var mensaje: String? = null

    @SerializedName("empresa_id")
    var empresaId: Int? = null

    @SerializedName("activo")
    var activo: Int? = null

    @SerializedName("tipo_notificacion_id")
    var tipoNotificacionId: Int? = null

    // Local-only helper
    var localId: Int = 0

    override fun toString(): String {
        return "Notificacion(nombre=$nombre, mensaje=$mensaje)"
    }
}
