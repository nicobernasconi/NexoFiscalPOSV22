// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/LocalidadRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.LocalidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class LocalidadRepository(private val dao: LocalidadDao) {

    fun getLocalidadesPaginated(query: String): Flow<PagingData<LocalidadEntity>> {
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

    suspend fun porId(id: Int): LocalidadEntity? = dao.getById(id)
    suspend fun guardar(localidad: LocalidadEntity) = dao.insert(localidad)
    suspend fun actualizar(localidad: LocalidadEntity) = dao.update(localidad)
    suspend fun eliminar(entity: LocalidadEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}