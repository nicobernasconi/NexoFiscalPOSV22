// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/SucursalRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SucursalDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class SucursalRepository(private val dao: SucursalDao) {

    fun getSucursalesPaginated(query: String): Flow<PagingData<SucursalEntity>> {
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

    suspend fun porId(id: Int): SucursalEntity? = dao.getById(id)
    suspend fun guardar(suc: SucursalEntity) = dao.insert(suc)
    suspend fun actualizar(suc: SucursalEntity) = dao.update(suc)
    suspend fun eliminar(entity: SucursalEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}