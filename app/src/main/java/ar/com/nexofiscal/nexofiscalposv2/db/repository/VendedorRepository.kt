// main/java/ar/com/nexofiscal/nexofiscalposv2/repository/VendedorRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.VendedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity
import kotlinx.coroutines.flow.Flow

class VendedorRepository(private val dao: VendedorDao) {

    fun getVendedoresPaginated(query: String): Flow<PagingData<VendedorEntity>> {
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

    suspend fun porId(id: Int): VendedorEntity? = dao.getById(id)
    suspend fun guardar(item: VendedorEntity) = dao.insert(item)
    suspend fun actualizar(item: VendedorEntity) = dao.update(item)
    suspend fun eliminar(entity: VendedorEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}