package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoDocumento {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "TipoDocumento{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}

