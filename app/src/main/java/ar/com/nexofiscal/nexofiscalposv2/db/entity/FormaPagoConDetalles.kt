package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de datos para obtener una FormaPago con su TipoFormaPago relacionado.
 * Room utiliza esta clase para unir las dos tablas en una sola consulta eficiente.
 */
data class FormaPagoConDetalles(
    @Embedded
    val formaPago: FormaPagoEntity,

    @Relation(
        parentColumn = "tipoFormaPagoId", // Clave foránea en FormaPagoEntity
        entityColumn = "serverId"        // Clave primaria en TipoFormaPagoEntity
    )
    val tipoFormaPago: TipoFormaPagoEntity?
)