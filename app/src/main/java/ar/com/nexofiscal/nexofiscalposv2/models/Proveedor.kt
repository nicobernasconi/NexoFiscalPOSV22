package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la entidad Proveedor.
 */
class Proveedor {
    // --- Getters & Setters ---
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
    var tipoIva: TipoIVA? = null

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
}
