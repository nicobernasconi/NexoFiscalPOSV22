// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ProveedorEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class ProveedorEntity(
    @PrimaryKey val id: Int,
    val razonSocial: String?,
    val direccion: String?,
    val localidadId: Int?,      // FK a LocalidadEntity.id
    val telefono: String?,
    val email: String?,
    val tipoIvaId: Int?,        // FK a TipoIVAEntity.id
    val cuit: String?,
    val categoriaId: Int?,      // FK a CategoriaEntity.id
    val subcategoriaId: Int?,   // FK a CategoriaEntity.id
    val fechaUltimaCompra: String?,
    val fechaUltimoPago: String?,
    val saldoActual: Double
)
