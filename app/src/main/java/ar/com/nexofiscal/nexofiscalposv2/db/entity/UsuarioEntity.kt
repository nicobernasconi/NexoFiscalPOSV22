// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/UsuarioEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Int,
    val nombreUsuario: String?,
    val password: String?,
    val nombreCompleto: String?,
    val activo: Int?,
    val empresaId: Int,
    val rolId: Int?,
    val sucursalId: Int?,
    val vendedorId: Int?
)
