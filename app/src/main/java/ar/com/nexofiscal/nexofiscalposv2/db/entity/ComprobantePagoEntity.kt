package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comprobante_pagos",
    // La clave primaria es una combinación del comprobante y la forma de pago
    primaryKeys = ["comprobanteLocalId", "formaPagoId"],
    foreignKeys = [
        ForeignKey(
            entity = ComprobanteEntity::class,
            parentColumns = ["id"], // Se enlaza con el ID local del comprobante
            childColumns = ["comprobanteLocalId"],
            onDelete = ForeignKey.CASCADE // Si se borra el comprobante, se borran sus pagos
        )
    ],
    indices = [Index(value = ["comprobanteLocalId"])]
)
data class ComprobantePagoEntity(
    val comprobanteLocalId: Long,
    val formaPagoId: Int, // El ID de la Forma de Pago
    val importe: Double,  // El monto pagado con esa forma
    var syncStatus: SyncStatus = SyncStatus.CREATED // Para gestionar la subida futura
)