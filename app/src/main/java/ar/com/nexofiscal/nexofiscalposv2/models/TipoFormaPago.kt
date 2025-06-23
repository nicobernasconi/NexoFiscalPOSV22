// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/TipoFormaPago.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class TipoFormaPago {
    // Getters & Setters
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "TipoFormaPago{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre): TipoFormaPago {
        val tipoFormaPago = TipoFormaPago()
        tipoFormaPago.id = id
        tipoFormaPago.nombre = nombre
        return tipoFormaPago
    }
}

