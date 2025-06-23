// main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ComprobanteConDetallesEntity.kt

package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ComprobanteConDetallesEntity(
    @Embedded
    val comprobante: ComprobanteEntity,

    @Relation(
        parentColumn = "clienteId",
        // CAMBIO AQUÍ: Debe apuntar a 'serverId' en la tabla de clientes
        entityColumn = "serverId"
    )
    val cliente: ClienteEntity?,

    @Relation(
        parentColumn = "tipoComprobanteId",
        // CAMBIO AQUÍ: Debe apuntar a 'serverId' en la tabla de tipos de comprobante
        entityColumn = "serverId"
    )
    val tipoComprobante: TipoComprobanteEntity?
)