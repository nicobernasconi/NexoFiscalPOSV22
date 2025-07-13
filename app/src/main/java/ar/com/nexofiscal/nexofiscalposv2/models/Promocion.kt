package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Promocion {
    var localId: Int = 0
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("descripcion")
    var descripcion: String? = null
    @SerializedName("porcentaje")
    var porcentaje: Int = 0

    override fun toString(): String {
        return "Promocion(nombre='$nombre', porcentaje=$porcentaje)"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        nombre: String? = this.nombre,
        descripcion: String? = this.descripcion,
        porcentaje: Int = this.porcentaje
    ): Promocion {
        val promocion = Promocion()
        promocion.localId = localId // <-- AÑADIDO
        promocion.id = id
        promocion.nombre = nombre
        promocion.descripcion = descripcion
        promocion.porcentaje = porcentaje
        return promocion
    }
}