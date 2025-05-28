// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoComprobanteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity

class TipoComprobanteRepository(private val dao: TipoComprobanteDao) {

    /** Flujo con todos los tipos de comprobante */
    fun todos(): Flow<List<TipoComprobanteEntity>> = dao.getAll()

    /** Obtiene un tipo por su id */
    suspend fun porId(id: Int): TipoComprobanteEntity? = dao.getById(id)

    /** Inserta o reemplaza un registro */
    suspend fun guardar(item: TipoComprobanteEntity) = dao.insert(item)

    /** Actualiza un registro existente */
    suspend fun actualizar(item: TipoComprobanteEntity) = dao.update(item)

    /** Elimina un registro */
    suspend fun eliminar(item: TipoComprobanteEntity) = dao.delete(item)

    /** Borra todos los registros */
    suspend fun eliminarTodo() = dao.clearAll()
}
