// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/MonedaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.MonedaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class MonedaRepository(private val dao: MonedaDao) {

    fun getMonedasPaginated(query: String): Flow<PagingData<MonedaEntity>> {
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

    suspend fun porId(id: Int): MonedaEntity? = dao.getById(id)
    suspend fun guardar(m: MonedaEntity) = dao.insert(m)
    suspend fun actualizar(m: MonedaEntity) = dao.update(m)
    suspend fun eliminar(entity: MonedaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}