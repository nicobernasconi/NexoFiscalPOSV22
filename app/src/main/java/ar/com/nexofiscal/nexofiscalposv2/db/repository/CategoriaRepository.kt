// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/CategoriaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity

class CategoriaRepository(private val dao: CategoriaDao) {

    fun todas(): Flow<List<CategoriaEntity>> = dao.getAll()

    suspend fun porId(id: Int): CategoriaEntity? = dao.getById(id)

    suspend fun guardar(entity: CategoriaEntity) = dao.insert(entity)

    suspend fun actualizar(entity: CategoriaEntity) = dao.update(entity)

    suspend fun eliminar(entity: CategoriaEntity) = dao.delete(entity)

    suspend fun eliminarTodo() = dao.clearAll()
}
