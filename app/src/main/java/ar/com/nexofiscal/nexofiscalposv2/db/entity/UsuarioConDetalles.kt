package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UsuarioConDetalles(
    @Embedded
    val usuario: UsuarioEntity,

    @Relation(
        parentColumn = "rolId",
        entityColumn = "serverId"
    )
    val rol: RolEntity?,

    @Relation(
        parentColumn = "sucursalId",
        entityColumn = "serverId"
    )
    val sucursal: SucursalEntity?,

    @Relation(
        parentColumn = "vendedorId",
        entityColumn = "serverId"
    )
    val vendedor: VendedorEntity?
)