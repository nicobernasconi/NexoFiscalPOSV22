// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/SucursalEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sucursales")
data class SucursalEntity(
    @PrimaryKey val id: Int,
    val empresaId: Int,
    val nombre: String?,
    val direccion: String?,
    val telefono: String?,
    val email: String?,
    val contactoNombre: String?,
    val contactoTelefono: String?,
    val contactoEmail: String?,
    val referenteNombre: String?,
    val referenteTelefono: String?,
    val referenteEmail: String?
)
