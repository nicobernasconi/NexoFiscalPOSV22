// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/CombinacionRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CombinacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity

class CombinacionRepository(private val dao: CombinacionDao) {

    /** Flujo con todas las combinaciones */
    fun todas(): Flow<List<CombinacionEntity>> = dao.getAll()

    /** Obten una combinación por su UID */
    suspend fun porUid(uid: Int): CombinacionEntity? = dao.getByUid(uid)

    /** Inserta o reemplaza */
    suspend fun guardar(entity: CombinacionEntity) = dao.insert(entity)

    /** Actualiza existente */
    suspend fun actualizar(entity: CombinacionEntity) = dao.update(entity)

    /** Elimina */
    suspend fun eliminar(entity: CombinacionEntity) = dao.delete(entity)

    /** Limpia la tabla */
    suspend fun eliminarTodo() = dao.clearAll()
}
