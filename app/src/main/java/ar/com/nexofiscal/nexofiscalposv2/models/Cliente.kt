package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Cliente {
    @SerializedName("id")
    var id: Int = 0
    var localId: Int = 0
    @SerializedName("tipo_iva_id")
    var tipoIvaId: Int? = null
    @SerializedName("nro_cliente")
    var nroCliente: Int = 0
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("cuit")
    var cuit: String? = null
    @SerializedName("tipo_documento")
    var tipoDocumento: TipoDocumento? = null
    @SerializedName("numero_documento")
    var numeroDocumento: String? = null
    @SerializedName("direccion_comercial")
    var direccionComercial: String? = null
    @SerializedName("direccion_entrega")
    var direccionEntrega: String? = null
    @SerializedName("localidad")
    var localidad: Localidad? = null
    @SerializedName("telefono")
    var telefono: String? = null
    @SerializedName("celular")
    var celular: String? = null
    @SerializedName("email")
    var email: String? = null
    @SerializedName("contacto")
    var contacto: String? = null
    @SerializedName("telefono_contacto")
    var telefonoContacto: String? = null
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
    var fechaUltimaCompra: String? = null
    @SerializedName("fecha_ultimo_pago")
    var fechaUltimoPago: String? = null
    @SerializedName("percepcion_iibb")
    var percepcionIibb: Double? = null
    @SerializedName("desactivado")
    var desactivado: Boolean? = null
    @SerializedName("tipo_iva")
    var tipoIva: TipoIVA? = null
    @SerializedName("provincia")
    var provincia: Provincia? = null

    override fun toString(): String {
        return "Cliente(nroCliente=$nroCliente,${tipoIva}, cuit=$cuit, nombre=$nombre)"
    }

    fun copy(
        id: Int = this.id,
        localId: Int = this.localId,
        tipoIvaId: Int? = this.tipoIvaId,
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
        cliente.localId = localId
        cliente.tipoIvaId = tipoIvaId
        cliente.nroCliente = nroCliente
        cliente.nombre = nombre
        cliente.cuit = cuit
        cliente.tipoDocumento = tipoDocumento?.copy()
        cliente.numeroDocumento = numeroDocumento
        cliente.direccionComercial = direccionComercial
        cliente.direccionEntrega = direccionEntrega
        cliente.localidad = localidad?.copy()
        cliente.telefono = telefono
        cliente.celular = celular
        cliente.email = email
        cliente.contacto = contacto
        cliente.telefonoContacto = telefonoContacto
        cliente.categoria = categoria?.copy()
        cliente.vendedores = vendedores?.copy()
        cliente.porcentajeDescuento = porcentajeDescuento
        cliente.limiteCredito = limiteCredito
        cliente.saldoInicial = saldoInicial
        cliente.saldoActual = saldoActual
        cliente.fechaUltimaCompra = fechaUltimaCompra
        cliente.fechaUltimoPago = fechaUltimoPago
        cliente.percepcionIibb = percepcionIibb
        cliente.desactivado = desactivado
        cliente.tipoIva = tipoIva?.copy()
        cliente.provincia = provincia?.copy()
        return cliente
    }
}