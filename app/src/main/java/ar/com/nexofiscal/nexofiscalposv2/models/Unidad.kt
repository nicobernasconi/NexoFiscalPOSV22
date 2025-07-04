package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Unidad {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    // getters & setters...
    @SerializedName("simbolo")
    var simbolo: String? = null

    override fun toString(): String {
        return "Unidad{" +
                "nombre='" + nombre + '\'' +
                ", simbolo='" + simbolo + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre, simbolo: String? = this.simbolo): Unidad {
        val unidad = Unidad()
        unidad.id = id
        unidad.nombre = nombre
        unidad.simbolo = simbolo
        return unidad
    }
}
