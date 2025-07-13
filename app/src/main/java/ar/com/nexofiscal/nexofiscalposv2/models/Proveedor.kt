package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Proveedor {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("razon_social")
    var razonSocial: String? = null
    @SerializedName("direccion")
    var direccion: String? = null
    @SerializedName("localidad")
    var localidad: Localidad? = null
    @SerializedName("telefono")
    var telefono: String? = null
    @SerializedName("email")
    var email: String? = null
    @SerializedName("tipo_iva")
    var tipoIva: TipoIVA? = null // Nota: en el modelo original es TipoIVA, no TipoIva
    @SerializedName("cuit")
    var cuit: String? = null
    @SerializedName("categoria")
    var categoria: Categoria? = null
    @SerializedName("subcategoria")
    var subcategoria: Categoria? = null
    @SerializedName("fecha_ultima_compra")
    var fechaUltimaCompra: String? = null
    @SerializedName("fecha_ultimo_pago")
    var fechaUltimoPago: String? = null
    @SerializedName("saldo_actual")
    var saldoActual: Double = 0.0

    override fun toString(): String {
        return "Proveedor(cuit=$cuit, razonSocial=$razonSocial)"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        razonSocial: String? = this.razonSocial,
        direccion: String? = this.direccion,
        localidad: Localidad? = this.localidad,
        telefono: String? = this.telefono,
        email: String? = this.email,
        tipoIva: TipoIVA? = this.tipoIva,
        cuit: String? = this.cuit,
        categoria: Categoria? = this.categoria,
        subcategoria: Categoria? = this.subcategoria,
        fechaUltimaCompra: String? = this.fechaUltimaCompra,
        fechaUltimoPago: String? = this.fechaUltimoPago,
        saldoActual: Double = this.saldoActual
    ): Proveedor {
        val proveedor = Proveedor()
        proveedor.localId = localId // <-- AÑADIDO
        proveedor.id = id
        proveedor.razonSocial = razonSocial
        proveedor.direccion = direccion
        proveedor.localidad = localidad?.copy()
        proveedor.telefono = telefono
        proveedor.email = email
        proveedor.tipoIva = tipoIva?.copy()
        proveedor.cuit = cuit
        proveedor.categoria = categoria?.copy()
        proveedor.subcategoria = subcategoria?.copy()
        proveedor.fechaUltimaCompra = fechaUltimaCompra
        proveedor.fechaUltimoPago = fechaUltimoPago
        proveedor.saldoActual = saldoActual
        return proveedor
    }
}