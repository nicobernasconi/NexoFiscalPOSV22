package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class ComprobanteRepository(private val dao: ComprobanteDao) {

    fun getComprobantesPaginated(query: String): Flow<PagingData<ComprobanteConDetalles>> {
        val normalizedQuery = "%${query.trim()}%"
        return Pager(
            config = PagingConfig(pageSize = 200, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    dao.getPagingSourceWithDetails()
                } else {
                    dao.searchPagingSourceWithDetails(normalizedQuery)
                }
            }
        ).flow
    }

    suspend fun porId(id: Int): ComprobanteEntity? = dao.getById(id)


    suspend fun getHighestNumeroForTipo(tipoId: Int): Int? = dao.getHighestNumeroForTipo(tipoId)

    suspend fun guardar(entity: ComprobanteEntity): Long = dao.insert(entity)
    suspend fun actualizar(entity: ComprobanteEntity) = dao.update(entity)
    suspend fun eliminar(entity: ComprobanteEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}