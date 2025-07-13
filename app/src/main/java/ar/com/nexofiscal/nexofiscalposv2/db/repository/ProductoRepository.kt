// main/java/ar/com/nexofiscal/nexofiscalposv2/db/repository/ProductoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val dao: ProductoDao) {

    fun getProductosPaginated(query: String): Flow<PagingData<ProductoConDetalles>> {
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

    // Asegúrate de que este método, que ya existe en el DAO, esté expuesto aquí.


    suspend fun getConDetallesById(id: Int): ProductoConDetalles? = dao.getConDetallesById(id)
    fun getFavoritosWithDetails(): Flow<List<ProductoConDetalles>> = dao.getFavoritosWithDetails()

    suspend fun findByBarcode(barcode: String): ProductoEntity? {
        return dao.findByBarcode(barcode)
    }
    suspend fun porId(id: Int): ProductoEntity? = dao.getById(id)
    fun getFavoritos(): Flow<List<ProductoEntity>> = dao.getFavoritos()
    suspend fun guardar(p: ProductoEntity) = dao.insert(p)
    suspend fun actualizar(p: ProductoEntity) = dao.update(p)
    suspend fun eliminar(entity: ProductoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}

