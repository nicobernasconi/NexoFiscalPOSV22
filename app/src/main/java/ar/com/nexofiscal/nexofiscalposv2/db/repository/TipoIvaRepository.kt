// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoIvaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity

class TipoIvaRepository(private val dao: TipoIvaDao) {

    /** Flujo con todos los tipos de IVA */
    fun todos(): Flow<List<TipoIvaEntity>> = dao.getAll()

    /** Obtiene un tipo de IVA por su id */
    suspend fun porId(id: Int): TipoIvaEntity? = dao.getById(id)

    /** Inserta o actualiza */
    suspend fun guardar(item: TipoIvaEntity) = dao.insert(item)

    /** Actualiza existente */
    suspend fun actualizar(item: TipoIvaEntity) = dao.update(item)

    /** Elimina */
    suspend fun eliminar(item: TipoIvaEntity) = dao.delete(item)

    /** Borra todos */
    suspend fun eliminarTodo() = dao.clearAll()
}
