// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/repository/ComprobanteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity

class ComprobanteRepository(private val dao: ComprobanteDao) {

    /** Flujo con todos los comprobantes */
    fun todos(): Flow<List<ComprobanteEntity>> = dao.getAll()

    /** Obtén un comprobante por su ID */
    suspend fun porId(id: Int): ComprobanteEntity? = dao.getById(id)

    /** Inserta o reemplaza */
    suspend fun guardar(entity: ComprobanteEntity) = dao.insert(entity)

    /** Actualiza existente */
    suspend fun actualizar(entity: ComprobanteEntity) = dao.update(entity)

    /** Elimina */
    suspend fun eliminar(entity: ComprobanteEntity) = dao.delete(entity)

    /** Limpia la tabla */
    suspend fun eliminarTodo() = dao.clearAll()
}