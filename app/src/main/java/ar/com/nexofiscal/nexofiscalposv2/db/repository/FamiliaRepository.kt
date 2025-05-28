// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/FamiliaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FamiliaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity

class FamiliaRepository(private val dao: FamiliaDao) {

    /** Flujo con todas las familias */
    fun todas(): Flow<List<FamiliaEntity>> = dao.getAll()

    /** Obtiene una familia por id */
    suspend fun porId(id: Int): FamiliaEntity? = dao.getById(id)

    /** Inserta o reemplaza una familia */
    suspend fun guardar(entity: FamiliaEntity) = dao.insert(entity)

    /** Actualiza una familia existente */
    suspend fun actualizar(entity: FamiliaEntity) = dao.update(entity)

    /** Elimina una familia */
    suspend fun eliminar(entity: FamiliaEntity) = dao.delete(entity)

    /** Borra todas las familias */
    suspend fun eliminarTodo() = dao.clearAll()
}
