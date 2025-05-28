package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Promocion {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("descripcion")
    var descripcion: String? = null // puede ser null

    @SerializedName("porcentaje")
    var porcentaje: Int = 0

    override fun toString(): String {
        return "Promocion{" +
                "nombre='" + nombre + '\'' +
                ", porcentaje=" + porcentaje +
                '}'
    }
}

