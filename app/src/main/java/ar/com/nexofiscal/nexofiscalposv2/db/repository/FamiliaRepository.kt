// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/FamiliaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FamiliaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class FamiliaRepository(private val dao: FamiliaDao) {

    fun getFamiliasPaginated(query: String): Flow<PagingData<FamiliaEntity>> {
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

    suspend fun porId(id: Int): FamiliaEntity? = dao.getById(id)
    suspend fun guardar(entity: FamiliaEntity) = dao.insert(entity)
    suspend fun actualizar(entity: FamiliaEntity) = dao.update(entity)
    suspend fun eliminar(entity: FamiliaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}