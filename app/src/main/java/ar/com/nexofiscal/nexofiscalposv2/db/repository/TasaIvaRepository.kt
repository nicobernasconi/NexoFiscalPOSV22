// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TasaIvaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TasaIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import kotlinx.coroutines.flow.Flow

class TasaIvaRepository(private val dao: TasaIvaDao) {

    fun getTasasIvaPaginated(query: String): Flow<PagingData<TasaIvaEntity>> {
        val normalizedQuery = "%${query.trim()}%"
        return Pager(
            config = PagingConfig(pageSize = 200, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    dao.getPagingSource()
                } else {
                    dao.searchPagingSource(normalizedQuery)
                }
            }
        ).flow
    }

    suspend fun porId(id: Int): TasaIvaEntity? = dao.getById(id)
    suspend fun guardar(item: TasaIvaEntity) = dao.insert(item)
    suspend fun actualizar(item: TasaIvaEntity) = dao.update(item)
    suspend fun eliminar(entity: TasaIvaEntity) {
        val serverId = entity.serverId
        if (serverId != null) {
            val refs = dao.countProductosReferencingTasaIva(serverId)
            if (refs > 0) throw IllegalStateException("No se puede borrar: hay $refs producto(s) que usan esta tasa de IVA.")
        }
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}