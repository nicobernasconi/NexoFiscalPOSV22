// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/AgrupacionEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agrupaciones")
data class AgrupacionEntity(
    @PrimaryKey val id: Int,
    val numero: Int?,
    val nombre: String?,
    val color: String?,
    val icono: String?
)
