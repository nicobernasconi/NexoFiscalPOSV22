// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity
import kotlinx.coroutines.flow.Flow

class TipoRepository(private val dao: TipoDao) {

    fun getTiposPaginated(query: String): Flow<PagingData<TipoEntity>> {
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

    suspend fun porId(id: Int): TipoEntity? = dao.getById(id)
    suspend fun guardar(item: TipoEntity) = dao.insert(item)
    suspend fun actualizar(item: TipoEntity) = dao.update(item)
    suspend fun eliminar(entity: TipoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}
