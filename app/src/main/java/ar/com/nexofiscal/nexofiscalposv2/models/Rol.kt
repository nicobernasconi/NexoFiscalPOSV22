package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Rol {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("descripcion")
    var descripcion: String? = null

    override fun toString(): String {
        return "Rol{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}
