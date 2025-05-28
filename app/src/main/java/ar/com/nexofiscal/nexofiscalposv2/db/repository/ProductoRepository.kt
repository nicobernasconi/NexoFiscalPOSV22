// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ProductoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity

class ProductoRepository(private val dao: ProductoDao) {

    /** Flujo con todos los productos */
    fun todos(): Flow<List<ProductoEntity>> = dao.getAll()

    /** Obtiene un producto por su id */
    suspend fun porId(id: Int): ProductoEntity? = dao.getById(id)

    /** Inserta o reemplaza un producto */
    suspend fun guardar(p: ProductoEntity) = dao.insert(p)

    /** Actualiza un producto existente */
    suspend fun actualizar(p: ProductoEntity) = dao.update(p)

    /** Elimina un producto */
    suspend fun eliminar(p: ProductoEntity) = dao.delete(p)

    /** Borra todos los productos */
    suspend fun eliminarTodo() = dao.clearAll()
}
