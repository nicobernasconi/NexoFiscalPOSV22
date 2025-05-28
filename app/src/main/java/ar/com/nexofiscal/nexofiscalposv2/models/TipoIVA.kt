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
}
