// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ProveedorRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProveedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class ProveedorRepository(private val dao: ProveedorDao) {

    fun getProveedoresPaginated(query: String): Flow<PagingData<ProveedorEntity>> {
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

    suspend fun porId(id: Int): ProveedorEntity? = dao.getById(id)
    suspend fun guardar(p: ProveedorEntity) = dao.insert(p)
    suspend fun actualizar(p: ProveedorEntity) = dao.update(p)
    suspend fun eliminar(entity: ProveedorEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}