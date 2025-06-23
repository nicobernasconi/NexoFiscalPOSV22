import androidx.room.Embedded
import androidx.room.Relation
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity

data class LocalidadConDetalles(
    @Embedded val localidad: LocalidadEntity,
    @Relation(parentColumn = "provinciaId", entityColumn = "serverId") val provincia: ProvinciaEntity?
)