// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/SubcategoriaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategorias")
data class SubcategoriaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val seImprime: Boolean?
)
