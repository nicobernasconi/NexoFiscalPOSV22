package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_gastos")
data class TipoGastoEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)

