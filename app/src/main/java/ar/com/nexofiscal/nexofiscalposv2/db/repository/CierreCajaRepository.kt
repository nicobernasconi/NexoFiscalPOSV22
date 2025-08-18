// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/CierreCajaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CierreCajaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class CierreCajaRepository(private val dao: CierreCajaDao) {

    fun getCierresCajaPaginated(query: String): Flow<PagingData<CierreCajaEntity>> {
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

    suspend fun porId(id: Int): CierreCajaEntity? = dao.getById(id)
    suspend fun guardar(cierre: CierreCajaEntity): Long = dao.insert(cierre)
    suspend fun actualizar(cierre: CierreCajaEntity) = dao.update(cierre)
    suspend fun eliminar(entity: CierreCajaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}