// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/CategoriaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val dao: CategoriaDao) {

    fun getCategoriasPaginated(query: String): Flow<PagingData<CategoriaEntity>> {
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

    suspend fun porId(id: Int): CategoriaEntity? = dao.getById(id)
    suspend fun guardar(entity: CategoriaEntity) = dao.insert(entity)
    suspend fun actualizar(entity: CategoriaEntity) = dao.update(entity)
    suspend fun eliminar(entity: CategoriaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}