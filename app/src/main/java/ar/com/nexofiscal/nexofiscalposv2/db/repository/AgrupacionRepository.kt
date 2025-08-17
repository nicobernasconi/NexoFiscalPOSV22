// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/AgrupacionRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.AgrupacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class AgrupacionRepository(private val dao: AgrupacionDao) {

    fun getAgrupacionesPaginated(query: String): Flow<PagingData<AgrupacionEntity>> {
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

    suspend fun porId(id: Int): AgrupacionEntity? = dao.getById(id)
    suspend fun guardar(entity: AgrupacionEntity) = dao.insert(entity)
    suspend fun actualizar(entity: AgrupacionEntity) = dao.update(entity)
    suspend fun eliminar(entity: AgrupacionEntity) {
        val serverId = entity.serverId
        if (serverId != null) {
            val refs = dao.countProductosReferencingAgrupacion(serverId)
            if (refs > 0) throw IllegalStateException("No se puede borrar: hay $refs producto(s) que usan esta agrupación.")
        }
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity)
    }
    suspend fun eliminarTodo() = dao.clearAll()
}