// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/AgrupacionRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.AgrupacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity

class AgrupacionRepository(private val dao: AgrupacionDao) {

    fun todas(): Flow<List<AgrupacionEntity>> = dao.getAll()

    suspend fun porId(id: Int): AgrupacionEntity? = dao.getById(id)

    suspend fun guardar(entity: AgrupacionEntity) = dao.insert(entity)

    suspend fun actualizar(entity: AgrupacionEntity) = dao.update(entity)

    suspend fun eliminar(entity: AgrupacionEntity) = dao.delete(entity)

    suspend fun eliminarTodo() = dao.clearAll()
}
