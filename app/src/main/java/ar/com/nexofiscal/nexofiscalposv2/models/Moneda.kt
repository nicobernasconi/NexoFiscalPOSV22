package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Moneda {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("simbolo")
    var simbolo: String? = null

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("cotizacion")
    var cotizacion: Double = 0.0

    override fun toString(): String {
        return "Moneda{" +
                "simbolo='" + simbolo + '\'' +
                ", nombre='" + nombre + '\'' +
                '}'
    }
}
