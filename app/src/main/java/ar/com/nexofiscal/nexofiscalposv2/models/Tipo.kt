package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Tipo {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("numero")
    var numero: Int? = null // puede ser null

    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "Tipo{" +
                "numero=" + numero +
                ", nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, numero: Int? = this.numero, nombre: String? = this.nombre): Tipo {
        val tipo = Tipo()
        tipo.id = id
        tipo.numero = numero
        tipo.nombre = nombre
        return tipo
    }


}
