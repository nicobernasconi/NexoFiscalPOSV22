// Cliente.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Cliente {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nro_cliente")
    var nroCliente: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("cuit")
    var cuit: Long? = null // puede ser null

    @SerializedName("tipo_documento")
    var tipoDocumento: TipoDocumento? = null

    @SerializedName("numero_documento")
    var numeroDocumento: Long? = null // puede ser null

    @SerializedName("direccion_comercial")
    var direccionComercial: String? = null // puede ser null

    @SerializedName("direccion_entrega")
    var direccionEntrega: String? = null // puede ser null

    @SerializedName("localidad")
    var localidad: Localidad? = null

    @SerializedName("telefono")
    var telefono: String? = null // puede ser null

    @SerializedName("celular")
    var celular: String? = null // puede ser null

    @SerializedName("email")
    var email: String? = null // puede ser null

    @SerializedName("contacto")
    var contacto: String? = null // puede ser null

    @SerializedName("telefono_contacto")
    var telefonoContacto: String? = null // puede ser null

    @SerializedName("categoria")
    var categoria: Categoria? = null

    @SerializedName("vendedores")
    var vendedores: Vendedor? = null

    @SerializedName("porcentaje_descuento")
    var porcentajeDescuento: Double? = null

    @SerializedName("limite_credito")
    var limiteCredito: Double? = null

    @SerializedName("saldo_inicial")
    var saldoInicial: Double? = null

    @SerializedName("saldo_actual")
    var saldoActual: Double? = null

    @SerializedName("fecha_ultima_compra")
    var fechaUltimaCompra: String? = null // puede ser null

    @SerializedName("fecha_ultimo_pago")
    var fechaUltimoPago: String? = null // puede ser null

    @SerializedName("percepcion_iibb")
    var percepcionIibb: Double? = null

    @SerializedName("desactivado")
    var desactivado: Boolean? = null // puede ser null

    @SerializedName("tipo_iva")
    var tipoIva: TipoIVA? = null

    @SerializedName("provincia")
    var provincia: Provincia? = null

    override fun toString(): String {
        return "Cliente{" +
                "nroCliente=" + nroCliente +
                ", nombre='" + nombre + '\'' +
                ", cuit=" + cuit +
                ", tipoDocumento=" + tipoDocumento +
                ", numeroDocumento=" + numeroDocumento +
                ", direccionEntrega='" + direccionEntrega + '\'' +
                '}'
    }
}

