package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de datos para obtener un Comprobante con todas sus entidades relacionadas.
 * Room utiliza esta clase para unir las tablas en una sola consulta eficiente.
 */
data class ComprobanteConDetalles(
    @Embedded
    val comprobante: ComprobanteEntity,

    @Relation(
        parentColumn = "clienteId",
        entityColumn = "serverId"
    )
    val cliente: ClienteEntity?,

    @Relation(
        parentColumn = "vendedorId",
        entityColumn = "serverId"
    )
    val vendedor: VendedorEntity?,

    @Relation(
        parentColumn = "tipoComprobanteId",
        entityColumn = "id" // Asumiendo que la clave en TipoComprobanteEntity es 'id' del servidor
    )
    val tipoComprobante: TipoComprobanteEntity?
)