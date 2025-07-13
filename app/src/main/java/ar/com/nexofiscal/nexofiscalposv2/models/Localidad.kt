package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Localidad {
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("codigo_postal")
    var codigoPostal: String? = null
    @SerializedName("provincia")
    var provincia: Provincia? = null

    override fun toString(): String {
        return "Localidad(nombre='$nombre')"
    }

    fun copy(
        id: Int = this.id,
        nombre: String? = this.nombre,
        codigoPostal: String? = this.codigoPostal,
        provincia: Provincia? = this.provincia
    ): Localidad {
        val localidad = Localidad()
        localidad.id = id
        localidad.nombre = nombre
        localidad.codigoPostal = codigoPostal
        localidad.provincia = provincia?.copy()
        return localidad
    }
}