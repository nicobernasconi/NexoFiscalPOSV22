package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Unidad {
    var localId: Int = 0
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("simbolo")
    var simbolo: String? = null

    override fun toString(): String {
        return "Unidad(nombre='$nombre', simbolo='$simbolo')"
    }

    fun copy(
        localId: Int = this.localId,
        id: Int = this.id,
        nombre: String? = this.nombre,
        simbolo: String? = this.simbolo
    ): Unidad {
        val unidad = Unidad()
        unidad.localId = localId
        unidad.id = id
        unidad.nombre = nombre
        unidad.simbolo = simbolo
        return unidad
    }
}