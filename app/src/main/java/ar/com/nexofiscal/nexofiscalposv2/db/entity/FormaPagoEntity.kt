// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/FormaPagoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para las formas de pago.
 * Almacenamos aquí solo el ID de tipoFormaPago; si quieres la relación completa,
 * define también TipoFormaPagoEntity y usa @ForeignKey.
 */
@Entity(tableName = "formas_pago")
data class FormaPagoEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val porcentaje: Int,
    val tipoFormaPagoId: Int?
)
