// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoComprobanteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity
import kotlinx.coroutines.flow.Flow

class TipoComprobanteRepository(private val dao: TipoComprobanteDao) {

    fun getTiposComprobantePaginated(query: String): Flow<PagingData<TipoComprobanteEntity>> {
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

    suspend fun porId(id: Int): TipoComprobanteEntity? = dao.getById(id)
    suspend fun guardar(item: TipoComprobanteEntity) = dao.insert(item)
    suspend fun actualizar(item: TipoComprobanteEntity) = dao.update(item)
    suspend fun eliminar(entity: TipoComprobanteEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}