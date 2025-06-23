// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoDocumentoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDocumentoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity
import kotlinx.coroutines.flow.Flow

class TipoDocumentoRepository(private val dao: TipoDocumentoDao) {

    fun getTiposDocumentoPaginated(query: String): Flow<PagingData<TipoDocumentoEntity>> {
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

    suspend fun porId(id: Int): TipoDocumentoEntity? = dao.getById(id)
    suspend fun guardar(item: TipoDocumentoEntity) = dao.insert(item)
    suspend fun actualizar(item: TipoDocumentoEntity) = dao.update(item)
    suspend fun eliminar(entity: TipoDocumentoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}