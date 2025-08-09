// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/StockProductoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.StockProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

class StockProductoRepository(private val dao: StockProductoDao) {

    /** Flujo con todo el stock de productos */
    fun todos(): Flow<List<StockProductoEntity>> = dao.getAll()

    /** Obtiene un registro por su id */
    suspend fun porId(id: Int): StockProductoEntity? = dao.getById(id)

    /** Inserta o reemplaza un registro */
    suspend fun guardar(item: StockProductoEntity) = dao.insert(item)

    /** Actualiza un registro existente */
    suspend fun actualizar(item: StockProductoEntity) = dao.update(item)

    /** Elimina un registro */
    suspend fun eliminar(entity: StockProductoEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }

    /** Borra todos los registros */
    suspend fun eliminarTodo() = dao.clearAll()

    /** Obtiene un registro de stock por el ID del producto */
    suspend fun getByProductoId(productoId: Int): StockProductoEntity? {
        return dao.getById(productoId)
    }
}
