// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoFormaPagoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoFormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity
import kotlinx.coroutines.flow.Flow

class TipoFormaPagoRepository(private val dao: TipoFormaPagoDao) {

    fun getTiposFormaPagoPaginated(query: String): Flow<PagingData<TipoFormaPagoEntity>> {
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

    suspend fun porId(id: Int): TipoFormaPagoEntity? = dao.getById(id)
    suspend fun guardar(item: TipoFormaPagoEntity) = dao.insert(item)
    suspend fun actualizar(item: TipoFormaPagoEntity) = dao.update(item)
    suspend fun eliminar(entity: TipoFormaPagoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}