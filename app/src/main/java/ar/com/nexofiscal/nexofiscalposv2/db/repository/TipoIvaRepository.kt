// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoIvaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity
import kotlinx.coroutines.flow.Flow

class TipoIvaRepository(private val dao: TipoIvaDao) {

    fun getTiposIvaPaginated(query: String): Flow<PagingData<TipoIvaEntity>> {
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

    suspend fun porId(id: Int): TipoIvaEntity? = dao.getById(id)
    suspend fun guardar(item: TipoIvaEntity) = dao.insert(item)
    suspend fun actualizar(item: TipoIvaEntity) = dao.update(item)
    suspend fun eliminar(entity: TipoIvaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}