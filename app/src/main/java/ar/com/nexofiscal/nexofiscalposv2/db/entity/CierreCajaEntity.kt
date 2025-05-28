// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/CierreCajaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cierres_caja")
data class CierreCajaEntity(
    @PrimaryKey val id: Int,
    val fecha: String?,
    val totalVentas: Double?,
    val totalGastos: Double?,
    val efectivoInicial: Double?,
    val efectivoFinal: Double?,
    val tipoCajaId: Int?,
    val usuarioId: Int?    // guarda sólo el id del Usuario; puedes añadir ForeignKey si defines UsuarioEntity
)
