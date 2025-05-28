// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/CategoriaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val seImprime: Int?
)
