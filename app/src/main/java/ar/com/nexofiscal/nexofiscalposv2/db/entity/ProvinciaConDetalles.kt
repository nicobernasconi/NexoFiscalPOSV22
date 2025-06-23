package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProvinciaConDetalles(
    @Embedded
    val provincia: ProvinciaEntity,

    @Relation(
        parentColumn = "paisId",
        entityColumn = "serverId" // Asume que paisId en Provincia se relaciona con serverId en Pais
    )
    val pais: PaisEntity?
)