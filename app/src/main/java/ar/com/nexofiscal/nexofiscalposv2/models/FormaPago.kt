package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class FormaPago {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("porcentaje")
    var porcentaje: Int = 0
    @SerializedName("activo")
    var activa: Int = 1
    @SerializedName("tipo_forma_pago")
    var tipoFormaPago: TipoFormaPago? = null


    override fun toString(): String {
        return "FormaPago(nombre='$nombre', porcentaje=$porcentaje)"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        nombre: String? = this.nombre,
        porcentaje: Int = this.porcentaje,
        activa: Int = this.activa,

        tipoFormaPago: TipoFormaPago? = this.tipoFormaPago
    ): FormaPago {
        val formaPago = FormaPago()
        formaPago.localId = localId // <-- AÑADIDO
        formaPago.id = id
        formaPago.nombre = nombre
        formaPago.porcentaje = porcentaje
        formaPago.activa = activa

        formaPago.tipoFormaPago = tipoFormaPago?.copy()
        return formaPago
    }
}