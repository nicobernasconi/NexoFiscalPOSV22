// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ProvinciaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProvinciaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity

class ProvinciaRepository(private val dao: ProvinciaDao) {

    /** Flujo con todas las provincias */
    fun todas(): Flow<List<ProvinciaEntity>> = dao.getAll()

    /** Obtiene una provincia por su id */
    suspend fun porId(id: Int): ProvinciaEntity? = dao.getById(id)

    /** Inserta o reemplaza una provincia */
    suspend fun guardar(p: ProvinciaEntity) = dao.insert(p)

    /** Actualiza una provincia existente */
    suspend fun actualizar(p: ProvinciaEntity) = dao.update(p)

    /** Elimina una provincia */
    suspend fun eliminar(p: ProvinciaEntity) = dao.delete(p)

    /** Borra todas las provincias */
    suspend fun eliminarTodo() = dao.clearAll()
}
