package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Agrupacion {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("numero")
    var numero: Int? = null
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("color")
    var color: String? = null
    @SerializedName("icono")
    var icono: String? = null

    override fun toString(): String {
        return "Agrupacion(id=$id, nombre='$nombre')"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        numero: Int? = this.numero,
        nombre: String? = this.nombre,
        color: String? = this.color,
        icono: String? = this.icono
    ): Agrupacion {
        val agrupacion = Agrupacion()
        agrupacion.localId = localId // <-- AÑADIDO
        agrupacion.id = id
        agrupacion.numero = numero
        agrupacion.nombre = nombre
        agrupacion.color = color
        agrupacion.icono = icono
        return agrupacion
    }
}