package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Provincia {
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("pais")
    var pais: Pais? = null

    override fun toString(): String {
        return "Provincia(nombre='$nombre')"
    }

    fun copy(
        id: Int = this.id,
        nombre: String? = this.nombre,
        pais: Pais? = this.pais
    ): Provincia {
        val provincia = Provincia()
        provincia.id = id
        provincia.nombre = nombre
        provincia.pais = pais?.copy()
        return provincia
    }
}