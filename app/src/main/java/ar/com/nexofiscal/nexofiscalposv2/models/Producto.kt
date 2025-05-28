package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Producto {
    // Getters & Setters omitted for brevity...
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("codigo")
    var codigo: String? = null

    @SerializedName("descripcion")
    var descripcion: String? = null

    @SerializedName("descripcion_ampliada")
    var descripcionAmpliada: String? = null

    @SerializedName("stock")
    var stock: Int = 0

    @SerializedName("stock_minimo")
    var stockMinimo: Int = 0

    @SerializedName("stock_pedido")
    var stockPedido: Int = 0

    @SerializedName("codigo_barra")
    var codigoBarra: String? = null

    @SerializedName("articulo_activado")
    var articuloActivado: Boolean? = null

    @SerializedName("producto_balanza")
    var productoBalanza: Int? = null

    @SerializedName("precio1")
    var precio1: Double = 0.0

    @SerializedName("precio2")
    var precio2: Double = 0.0

    @SerializedName("precio3")
    var precio3: Double = 0.0

    @SerializedName("precio4")
    var precio4: Double = 0.0

    @SerializedName("moneda")
    var moneda: Moneda? = null

    @SerializedName("tasa_iva")
    var tasaIva: TasaIva? = null

    @SerializedName("incluye_iva")
    var incluyeIva: Int? = null

    @SerializedName("impuesto_interno")
    var impuestoInterno: Double = 0.0

    @SerializedName("tipo_impuesto_interno")
    var tipoImpuestoInterno: Int = 0

    @SerializedName("precio1_impuesto_interno")
    var precio1ImpuestoInterno: Double = 0.0

    @SerializedName("precio2_impuesto_interno")
    var precio2ImpuestoInterno: Double = 0.0

    @SerializedName("precio3_impuesto_interno")
    var precio3ImpuestoInterno: Double = 0.0

    @SerializedName("precio_costo")
    var precioCosto: Double = 0.0

    @SerializedName("fraccionado")
    var fraccionado: Int? = null

    @SerializedName("rg5329_23")
    var rg5329_23: Int? = null

    @SerializedName("activo")
    var activo: Int = 0

    @SerializedName("texto_panel")
    var textoPanel: String? = null

    @SerializedName("iibb")
    var iibb: Double = 0.0

    @SerializedName("codigo_barra2")
    var codigoBarra2: String? = null

    @SerializedName("oferta")
    var oferta: Int? = null

    @SerializedName("margen_ganancia")
    var margenGanancia: Double = 0.0

    @SerializedName("favorito")
    var favorito: Int = 0

    @SerializedName("stock_actual")
    var stockActual: MutableList<StockProducto?>? = null

    @SerializedName("materia_prima")
    var materiaPrima: Any? = null

    @SerializedName("familia")
    var familia: Familia? = null


    @SerializedName("agrupacion")
    var agrupacion: Agrupacion? = null

    @SerializedName("proveedor")
    var proveedor: Proveedor? = null

    @SerializedName("tipo")
    var tipo: Tipo? = null

    @SerializedName("unidad")
    var unidad: Unidad? = null

    @SerializedName("combinaciones")
    var combinaciones: MutableList<Combinacion?>? = null
}
