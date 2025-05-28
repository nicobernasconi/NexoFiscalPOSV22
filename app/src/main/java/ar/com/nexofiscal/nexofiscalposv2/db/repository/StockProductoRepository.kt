// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/StockProductoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.StockProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity

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
    suspend fun eliminar(item: StockProductoEntity) = dao.delete(item)

    /** Borra todos los registros */
    suspend fun eliminarTodo() = dao.clearAll()
}
