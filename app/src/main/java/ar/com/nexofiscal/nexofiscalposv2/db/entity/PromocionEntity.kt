// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/PromocionEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promociones")
data class PromocionEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val descripcion: String?,
    val porcentaje: Int
)
