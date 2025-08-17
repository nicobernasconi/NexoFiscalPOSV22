package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.NotificacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

class NotificacionRepository(private val dao: NotificacionDao) {

    fun getNotificacionesPaginated(query: String): Flow<PagingData<NotificacionEntity>> {
        val normalizedQuery = "%${query.trim()}%"
        return Pager(
            config = PagingConfig(pageSize = 200, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) dao.getPagingSource() else dao.searchPagingSource(normalizedQuery)
            }
        ).flow
    }

    suspend fun upsertAll(items: List<NotificacionEntity>) = dao.upsertAll(items)
    suspend fun clearAll() = dao.clearAll()
}
