package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoComprobante {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("numero")
    var numero: Int? = null // puede ser null

    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "TipoComprobante{" +
                "numero=" + numero +
                ", nombre='" + nombre + '\'' +
                '}'
    }
}
