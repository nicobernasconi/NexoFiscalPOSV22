package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoIVA {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("letra_factura")
    var letraFactura: String? = null
    @SerializedName("porcentaje")
    var porcentaje: Double? = null

    override fun toString(): String {
        return "TipoIVA(nombre='$nombre')"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        nombre: String? = this.nombre,
        letraFactura: String? = this.letraFactura,
        porcentaje: Double? = this.porcentaje
    ): TipoIVA {
        val tipoIVA = TipoIVA()
        tipoIVA.localId = localId // <-- AÑADIDO
        tipoIVA.id = id
        tipoIVA.nombre = nombre
        tipoIVA.letraFactura = letraFactura
        tipoIVA.porcentaje = porcentaje
        return tipoIVA
    }
}