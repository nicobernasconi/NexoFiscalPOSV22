package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

class Producto {
    // Getters & Setters omitted for brevity...
    var localId: Int = 0

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

    @JsonAdapter(TasaIvaAdapter::class)
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
    override fun toString(): String {
        return "Producto(codigo=$codigo, descripcion=$descripcion)"
    }

    fun copy(localId: Int = this.localId,
             id: Int = this.id,
             codigo: String? = this.codigo,
             descripcion: String? = this.descripcion,
             descripcionAmpliada: String? = this.descripcionAmpliada,
             stock: Int = this.stock,
             stockMinimo: Int = this.stockMinimo,
             stockPedido: Int = this.stockPedido,
             codigoBarra: String? = this.codigoBarra,
             articuloActivado: Boolean? = this.articuloActivado,
             productoBalanza: Int? = this.productoBalanza,
             precio1: Double = this.precio1,
             precio2: Double = this.precio2,
             precio3: Double = this.precio3,
             precio4: Double = this.precio4,
             moneda: Moneda? = this.moneda?.copy(),
             tasaIva: TasaIva? = this.tasaIva?.copy(),
             incluyeIva: Int? = this.incluyeIva,
             impuestoInterno: Double = this.impuestoInterno,
             tipoImpuestoInterno: Int = this.tipoImpuestoInterno,
             precio1ImpuestoInterno: Double = this.precio1ImpuestoInterno,
             precio2ImpuestoInterno: Double = this.precio2ImpuestoInterno,
             precio3ImpuestoInterno: Double = this.precio3ImpuestoInterno,
             precioCosto: Double = this.precioCosto,
             fraccionado: Int? = this.fraccionado,
             rg5329_23: Int? = this.rg5329_23,
             activo: Int = this.activo,
             textoPanel: String? = this.textoPanel,
             iibb: Double = this.iibb,
             codigoBarra2: String? = this.codigoBarra2,
             oferta: Int? = this.oferta,
             margenGanancia: Double = this.margenGanancia,
             favorito: Int = this.favorito,
             stockActual: MutableList<StockProducto?>? = null, // No se copia por defecto
             materiaPrima: Any? = null, // No se copia por defecto
             familia: Familia? = null, // No se copia por defecto
             agrupacion: Agrupacion? = null, // No se copia por defecto
             proveedor: Proveedor? = null, // No se copia por defecto
             tipo: Tipo? = null, //
             unidad: Unidad? = null, // No se copia por defecto
             combinaciones: MutableList<Combinacion?>? = null // No se copia por defecto
    ): Producto {
        val producto = Producto()
        producto.id = id
        producto.localId = localId
        producto.codigo = codigo
        producto.descripcion = descripcion
        producto.descripcionAmpliada = descripcionAmpliada
        producto.stock = stock
        producto.stockMinimo = stockMinimo
        producto.stockPedido = stockPedido
        producto.codigoBarra = codigoBarra
        producto.articuloActivado = articuloActivado
        producto.productoBalanza = productoBalanza
        producto.precio1 = precio1
        producto.precio2 = precio2
        producto.precio3 = precio3
        producto.precio4 = precio4
        producto.moneda = moneda?.copy()
        producto.tasaIva = tasaIva?.copy()
        producto.incluyeIva = incluyeIva
        producto.impuestoInterno = impuestoInterno
        producto.tipoImpuestoInterno = tipoImpuestoInterno
        producto.precio1ImpuestoInterno = precio1ImpuestoInterno
        producto.precio2ImpuestoInterno = precio2ImpuestoInterno
        producto.precio3ImpuestoInterno = precio3ImpuestoInterno
        producto.precioCosto = precioCosto
        producto.fraccionado = fraccionado
        producto.rg5329_23 = rg5329_23
        producto.activo = activo
        producto.textoPanel = textoPanel
        producto.iibb = iibb
        producto.codigoBarra2 = codigoBarra2
        producto.oferta = oferta
        producto.margenGanancia = margenGanancia
        producto.favorito = favorito

        // No se copian los valores por defecto de las listas y objetos complejos.
        if (stockActual != null) {
            producto.stockActual = stockActual.map { it?.copy() }.toMutableList()
        }

        if (materiaPrima != null) {
            // Aquí podrías definir cómo copiar materiaPrima si es necesario.
            // Por ahora, lo dejamos como está.
            producto.materiaPrima = materiaPrima
        }

        if (familia != null) {
            // Aquí podrías definir cómo copiar familia si es necesario.
            // Por ahora, lo dejamos como está.
            producto.familia = familia.copy()
        }

        if (agrupacion != null) {
            // Aquí podrías definir cómo copiar agrupacion si es necesario.
            // Por ahora, lo dejamos como está.
            producto.agrupacion = agrupacion.copy()

        }
        if (proveedor != null) {
            // Aquí podrías definir cómo copiar proveedor si es necesario.
            // Por ahora, lo dejamos como está.
            producto.proveedor = proveedor.copy()
        }
        if (tipo != null) {
            // Aquí podrías definir cómo copiar tipo si es necesario.
            // Por ahora, lo dejamos como está.
            producto.tipo = tipo.copy()
        }
        if (unidad != null) {
            // Aquí podrías definir cómo copiar unidad si es necesario.
            // Por ahora, lo dejamos como está.
            producto.unidad = unidad.copy()
        }
        if (combinaciones != null) {
            producto.combinaciones = combinaciones.map { it?.copy() }.toMutableList()
        }
        return producto
    }
}