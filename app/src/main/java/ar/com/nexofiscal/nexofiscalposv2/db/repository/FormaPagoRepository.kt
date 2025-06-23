// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/FormaPagoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class FormaPagoRepository(private val dao: FormaPagoDao) {

    fun getFormasPagoPaginated(query: String): Flow<PagingData<FormaPagoEntity>> {
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
    fun getAll(): Flow<List<FormaPagoEntity>> = dao.getAll()
    suspend fun porId(id: Int): FormaPagoEntity? = dao.getById(id)
    suspend fun guardar(f: FormaPagoEntity) = dao.insert(f)
    suspend fun actualizar(f: FormaPagoEntity) = dao.update(f)
    suspend fun eliminar(entity: FormaPagoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}