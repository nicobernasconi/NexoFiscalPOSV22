// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/PromocionRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PromocionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class PromocionRepository(private val dao: PromocionDao) {

    fun getPromocionesPaginated(query: String): Flow<PagingData<PromocionEntity>> {
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

    suspend fun porId(id: Int): PromocionEntity? = dao.getById(id)
    suspend fun guardar(p: PromocionEntity) = dao.insert(p)
    suspend fun actualizar(p: PromocionEntity) = dao.update(p)
    suspend fun eliminar(entity: PromocionEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}