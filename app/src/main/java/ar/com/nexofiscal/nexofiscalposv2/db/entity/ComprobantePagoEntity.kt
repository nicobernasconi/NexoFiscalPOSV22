package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comprobante_pagos",
    primaryKeys = ["comprobanteLocalId"],
    foreignKeys = [
        ForeignKey(
            entity = ComprobanteEntity::class,
            parentColumns = ["id"],
            childColumns = ["comprobanteLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["comprobanteLocalId"])]
)
data class ComprobantePagoEntity(
    val comprobanteLocalId: Long,
    val formaPagoId: Int,
    val importe: Double,
    var syncStatus: SyncStatus // AÑADIDO
)