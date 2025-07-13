// main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ProductoConDetalles.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de datos para obtener un Producto con todas sus entidades relacionadas.
 * Room utiliza esta clase para unir las tablas en una sola consulta eficiente.
 */
data class ProductoConDetalles(
    @Embedded
    val producto: ProductoEntity,

    @Relation(parentColumn = "monedaId", entityColumn = "serverId")
    val moneda: MonedaEntity?,

    @Relation(parentColumn = "tasaIvaId", entityColumn = "serverId")
    val tasaIva: TasaIvaEntity?,

    @Relation(parentColumn = "familiaId", entityColumn = "serverId")
    val familia: FamiliaEntity?,

    @Relation(parentColumn = "agrupacionId", entityColumn = "serverId")
    val agrupacion: AgrupacionEntity?,

    @Relation(parentColumn = "proveedorId", entityColumn = "serverId")
    val proveedor: ProveedorEntity?,

    @Relation(parentColumn = "tipoId", entityColumn = "serverId")
    val tipo: TipoEntity?,

    @Relation(parentColumn = "unidadId", entityColumn = "serverId")
    val unidad: UnidadEntity?
)