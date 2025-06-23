// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ClienteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ClienteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class ClienteRepository(private val dao: ClienteDao) {

    fun getClientesPaginated(query: String): Flow<PagingData<ClienteEntity>> {
        val normalizedQuery = "%${query.trim()}%"
        return Pager(
            config = PagingConfig(
                pageSize = 200,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    dao.getPagingSource()
                } else {
                    dao.searchPagingSource(normalizedQuery)
                }
            }
        ).flow
    }

    suspend fun porId(id: Int): ClienteEntity? = dao.getById(id)

    suspend fun guardar(c: ClienteEntity) = dao.insert(c)

    suspend fun actualizar(c: ClienteEntity) = dao.update(c)

    suspend fun eliminar(entity: ClienteEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }

    suspend fun eliminarTodo() = dao.clearAll()
}