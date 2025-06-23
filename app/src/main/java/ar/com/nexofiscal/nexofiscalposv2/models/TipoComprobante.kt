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

    fun copy(id: Int = this.id, numero: Int? = this.numero, nombre: String? = this.nombre): TipoComprobante {
        val tipoComprobante = TipoComprobante()
        tipoComprobante.id = id
        tipoComprobante.numero = numero
        tipoComprobante.nombre = nombre
        return tipoComprobante
    }
}
