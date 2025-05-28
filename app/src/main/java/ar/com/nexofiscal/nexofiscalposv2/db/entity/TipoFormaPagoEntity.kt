// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TipoFormaPagoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_forma_pago")
data class TipoFormaPagoEntity(
    @PrimaryKey val id: Int,
    val nombre: String?
)
