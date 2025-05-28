// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/PaisRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PaisDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity

class PaisRepository(private val dao: PaisDao) {

    /** Flujo con todos los países */
    fun todos(): Flow<List<PaisEntity>> = dao.getAll()

    /** Obtiene un país por su id */
    suspend fun porId(id: Int): PaisEntity? = dao.getById(id)

    /** Inserta o reemplaza un país */
    suspend fun guardar(p: PaisEntity) = dao.insert(p)

    /** Actualiza un país existente */
    suspend fun actualizar(p: PaisEntity) = dao.update(p)

    /** Elimina un país */
    suspend fun eliminar(p: PaisEntity) = dao.delete(p)

    /** Borra todos los países */
    suspend fun eliminarTodo() = dao.clearAll()
}
