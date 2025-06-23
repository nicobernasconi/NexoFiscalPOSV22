// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/UnidadRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.UnidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

class UnidadRepository(private val dao: UnidadDao) {

    fun getUnidadesPaginated(query: String): Flow<PagingData<UnidadEntity>> {
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

    suspend fun porId(id: Int): UnidadEntity? = dao.getById(id)
    suspend fun guardar(item: UnidadEntity) = dao.insert(item)
    suspend fun actualizar(item: UnidadEntity) = dao.update(item)
    suspend fun eliminar(entity: UnidadEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}