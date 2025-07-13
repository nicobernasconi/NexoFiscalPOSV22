package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoDocumento {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "TipoDocumento(nombre='$nombre')"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        nombre: String? = this.nombre
    ): TipoDocumento {
        val tipoDocumento = TipoDocumento()
        tipoDocumento.localId = localId // <-- AÑADIDO
        tipoDocumento.id = id
        tipoDocumento.nombre = nombre
        return tipoDocumento
    }
}