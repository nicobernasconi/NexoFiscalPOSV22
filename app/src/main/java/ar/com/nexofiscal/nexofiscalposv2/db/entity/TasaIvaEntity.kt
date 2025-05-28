// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TasaIvaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasa_iva")
data class TasaIvaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val tasa: Double
)
