// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity

class TipoRepository(private val dao: TipoDao) {

    /** Flujo con todos los tipos */
    fun todos(): Flow<List<TipoEntity>> = dao.getAll()

    /** Obtiene un tipo por su id */
    suspend fun porId(id: Int): TipoEntity? = dao.getById(id)

    /** Inserta o reemplaza un tipo */
    suspend fun guardar(item: TipoEntity) = dao.insert(item)

    /** Actualiza un tipo existente */
    suspend fun actualizar(item: TipoEntity) = dao.update(item)

    /** Elimina un tipo */
    suspend fun eliminar(item: TipoEntity) = dao.delete(item)

    /** Borra todos los tipos */
    suspend fun eliminarTodo() = dao.clearAll()
}
