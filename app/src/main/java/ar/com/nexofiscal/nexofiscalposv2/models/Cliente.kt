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
    var cuit: String? = null // puede ser null

    @SerializedName("tipo_documento")
    var tipoDocumento: TipoDocumento? = null

    @SerializedName("numero_documento")
    var numeroDocumento: String? = null // puede ser null

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
        return "Cliente(nroCliente=$nroCliente, cuit=$cuit, nombre=$nombre)"
    }


fun copy(
    id: Int = this.id,
    nroCliente: Int = this.nroCliente,
    nombre: String? = this.nombre,
    cuit: String? = this.cuit,
    tipoDocumento: TipoDocumento? = this.tipoDocumento,
    numeroDocumento: String? = this.numeroDocumento,
    direccionComercial: String? = this.direccionComercial,
    direccionEntrega: String? = this.direccionEntrega,
    localidad: Localidad? = this.localidad,
    telefono: String? = this.telefono,
    celular: String? = this.celular,
    email: String? = this.email,
    contacto: String? = this.contacto,
    telefonoContacto: String? = this.telefonoContacto,
    categoria: Categoria? = this.categoria,
    vendedores: Vendedor? = this.vendedores,
    porcentajeDescuento: Double? = this.porcentajeDescuento,
    limiteCredito: Double? = this.limiteCredito,
    saldoInicial: Double? = this.saldoInicial,
    saldoActual: Double? = this.saldoActual,
    fechaUltimaCompra: String? = this.fechaUltimaCompra,
    fechaUltimoPago: String? = this.fechaUltimoPago,
    percepcionIibb: Double? = this.percepcionIibb,
    desactivado: Boolean? = this.desactivado,
    tipoIva: TipoIVA? = this.tipoIva,
    provincia: Provincia? = this.provincia
): Cliente {
    val cliente = Cliente()
    cliente.id = id
    cliente.nroCliente = nroCliente
    cliente.nombre = nombre
    cliente.cuit = cuit
    cliente.tipoDocumento = tipoDocumento
    cliente.numeroDocumento = numeroDocumento
    cliente.direccionComercial = direccionComercial
    cliente.direccionEntrega = direccionEntrega
    cliente.localidad = localidad
    cliente.telefono = telefono
    cliente.celular = celular
    cliente.email = email
    cliente.contacto = contacto
    cliente.telefonoContacto = telefonoContacto
    cliente.categoria = categoria
    cliente.vendedores = vendedores
    cliente.porcentajeDescuento = porcentajeDescuento
    cliente.limiteCredito = limiteCredito
    cliente.saldoInicial = saldoInicial
    cliente.saldoActual = saldoActual
    cliente.fechaUltimaCompra = fechaUltimaCompra
    cliente.fechaUltimoPago = fechaUltimoPago
    cliente.percepcionIibb = percepcionIibb
    cliente.desactivado = desactivado
    cliente.tipoIva = tipoIva
    cliente.provincia = provincia
    return cliente
}

}

