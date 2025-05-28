// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/UnidadRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.UnidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity

class UnidadRepository(private val dao: UnidadDao) {

    /** Flujo con todas las unidades */
    fun todas(): Flow<List<UnidadEntity>> = dao.getAll()

    /** Obtiene una por id */
    suspend fun porId(id: Int): UnidadEntity? = dao.getById(id)

    /** Inserta o reemplaza */
    suspend fun guardar(item: UnidadEntity) = dao.insert(item)

    /** Actualiza existente */
    suspend fun actualizar(item: UnidadEntity) = dao.update(item)

    /** Elimina */
    suspend fun eliminar(item: UnidadEntity) = dao.delete(item)

    /** Borra todas */
    suspend fun eliminarTodo() = dao.clearAll()
}
