// src/main/java/ar/com/nexofiscal/nexofiscalposv2/models/VentaActual.kt
package ar.com.nexofiscal.nexofiscalposv2.models

/**
 * Gestiona la venta actual: lista de productos vendidos y cálculos de totales e IVA.
 */
class VentaActual {

    /**
     * Representa un producto vendido en la venta actual.
     *
     * @param idProducto     Identificador único del producto.
     * @param descripcion    Descripción del producto.
     * @param precioUnitario Precio unitario (incluye IVA).
     * @param cantidad       Cantidad vendida.
     * @param descuentoTotal Descuento total aplicado a este ítem.
     * @param tasaIva        Tasa de IVA aplicada (por ejemplo, 0.21 para 21%).
     */
    data class ProductoVendido(
        val producto: Producto,
        val descripcion: String,
        val precioUnitario: Double,
        val cantidad: Int,
        val descuentoTotal: Double = 0.0,
        val tasaIva: Double = 0.21
    )

    /**
     * Resumen de totales para una tasa de IVA dada.
     *
     * @param tasa   Tasa de IVA (0.21, 0.105, 0.0, etc.).
     * @param neto   Monto neto (sin IVA).
     * @param iva    Monto de IVA.
     * @param bruto  Monto bruto (neto + IVA).
     */
    data class VatSummary(
        val tasa: Double,
        val neto: Double,
        val iva: Double,
        val bruto: Double
    )

    private val productos = mutableListOf<ProductoVendido>()

    /** Agrega un ítem a la venta actual. */
    fun agregarProducto(item: ProductoVendido) {
        productos += item
    }

    /** Elimina el ítem en la posición indicada. */
    fun eliminarProductoEn(pos: Int) {
        if (pos in productos.indices) productos.removeAt(pos)
    }

    /** Limpia todos los ítems de la venta. */
    fun clear() {
        productos.clear()
    }

    /** Devuelve la lista actual de productos vendidos. */
    fun getProductos(): List<ProductoVendido> = productos.toList()

    /** Total bruto (suma de precioUnitario * cantidad menos descuentos). */
    fun totalBruto(): Double =
        productos.sumOf { it.cantidad * it.precioUnitario - it.descuentoTotal }

    /** Total neto (suma de bruto / (1 + tasaIva)). */
    fun totalNeto(): Double =
        productos.sumOf { item ->
            val bruto = item.cantidad * item.precioUnitario - item.descuentoTotal
            bruto / (1 + item.tasaIva)
        }

    /** Total de IVA (totalBruto − totalNeto). */
    fun totalIva(): Double =
        totalBruto() - totalNeto()

    /**
     * Genera un resumen por cada tasa de IVA presente en los productos.
     *
     * @return Mapa donde la clave es la tasa de IVA y el valor es el resumen correspondiente.
     */
    fun resumenPorTasaIva(): Map<Double, VatSummary> =
        productos
            .groupBy { it.tasaIva }
            .mapValues { (tasa, lista) ->
                val bruto = lista.sumOf { it.cantidad * it.precioUnitario - it.descuentoTotal }
                val neto  = bruto / (1 + tasa)
                val iva   = bruto - neto
                VatSummary(tasa, neto, iva, bruto)
            }
}
