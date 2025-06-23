// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/FormaPago.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class FormaPago {
    // Getters & Setters
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("porcentaje")
    var porcentaje: Int = 0

    @SerializedName("tipo_forma_pago")
    var tipoFormaPago: TipoFormaPago? = null

    override fun toString(): String {
        return "FormaPago{" +
                "nombre='" + nombre + '\'' +
                ", porcentaje=" + porcentaje +
                '}'
    }
    fun copy(id: Int = this.id, nombre: String? = this.nombre, porcentaje: Int = this.porcentaje, tipoFormaPago: TipoFormaPago? = this.tipoFormaPago): FormaPago {
        val formaPago = FormaPago()
        formaPago.id = id
        formaPago.nombre = nombre
        formaPago.porcentaje = porcentaje
        formaPago.tipoFormaPago = tipoFormaPago?.copy() // Asumiendo que TipoFormaPago tiene un método copy()
        return formaPago
    }
}
