// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoFormaPagoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoFormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity

class TipoFormaPagoRepository(private val dao: TipoFormaPagoDao) {

    /** Flujo con todos los tipos de forma de pago */
    fun todos(): Flow<List<TipoFormaPagoEntity>> = dao.getAll()

    /** Obtiene uno por su id */
    suspend fun porId(id: Int): TipoFormaPagoEntity? = dao.getById(id)

    /** Guarda o reemplaza */
    suspend fun guardar(item: TipoFormaPagoEntity) = dao.insert(item)

    /** Actualiza */
    suspend fun actualizar(item: TipoFormaPagoEntity) = dao.update(item)

    /** Elimina */
    suspend fun eliminar(item: TipoFormaPagoEntity) = dao.delete(item)

    /** Borra todo */
    suspend fun eliminarTodo() = dao.clearAll()
}
