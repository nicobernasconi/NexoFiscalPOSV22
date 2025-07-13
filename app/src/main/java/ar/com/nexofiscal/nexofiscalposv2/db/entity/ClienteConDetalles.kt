// main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ClienteConDetalles.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de datos para obtener un Cliente con todas sus entidades relacionadas.
 * Room utiliza esta clase para unir las tablas en una sola consulta eficiente.
 */
data class ClienteConDetalles(
    @Embedded
    val cliente: ClienteEntity,

    @Relation(parentColumn = "tipoDocumentoId", entityColumn = "serverId")
    val tipoDocumento: TipoDocumentoEntity?,

    @Relation(parentColumn = "tipoIvaId", entityColumn = "serverId")
    val tipoIva: TipoIvaEntity?,

    @Relation(parentColumn = "localidadId", entityColumn = "serverId")
    val localidad: LocalidadEntity?,

    @Relation(parentColumn = "provinciaId", entityColumn = "serverId")
    val provincia: ProvinciaEntity?,

    @Relation(parentColumn = "categoriaId", entityColumn = "serverId")
    val categoria: CategoriaEntity?,

    @Relation(parentColumn = "vendedoresId", entityColumn = "serverId")
    val vendedor: VendedorEntity?
)