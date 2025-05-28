// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/MonedaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monedas")
data class MonedaEntity(
    @PrimaryKey val id: Int,
    val simbolo: String?,
    val nombre: String?,
    val cotizacion: Double
)
