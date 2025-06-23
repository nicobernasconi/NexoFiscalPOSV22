package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoIVA {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("letra_factura")
    var letraFactura: String? = null

    @SerializedName("porcentaje")
    var porcentaje: Double? = null // puede ser null

    override fun toString(): String {
        return "TipoIVA{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre, letraFactura: String? = this.letraFactura, porcentaje: Double? = this.porcentaje): TipoIVA {
        val tipoIVA = TipoIVA()
        tipoIVA.id = id
        tipoIVA.nombre = nombre
        tipoIVA.letraFactura = letraFactura
        tipoIVA.porcentaje = porcentaje
        return tipoIVA
    }
}
