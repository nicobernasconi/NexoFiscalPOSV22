// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/PaisRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PaisDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class PaisRepository(private val dao: PaisDao) {

    fun getPaisesPaginated(query: String): Flow<PagingData<PaisEntity>> {
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

    suspend fun porId(id: Int): PaisEntity? = dao.getById(id)
    suspend fun guardar(p: PaisEntity) = dao.insert(p)
    suspend fun actualizar(p: PaisEntity) = dao.update(p)
    suspend fun eliminar(entity: PaisEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}