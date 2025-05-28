// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/RenglonComprobanteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.RenglonComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity

class RenglonComprobanteRepository(private val dao: RenglonComprobanteDao) {

    fun todos(): Flow<List<RenglonComprobanteEntity>> = dao.getAll()

    fun porComprobante(compId: Int): Flow<List<RenglonComprobanteEntity>> =
        dao.getByComprobante(compId)

    suspend fun porId(id: Int): RenglonComprobanteEntity? = dao.getById(id)

    suspend fun guardar(entity: RenglonComprobanteEntity) = dao.insert(entity)

    suspend fun actualizar(entity: RenglonComprobanteEntity) = dao.update(entity)

    suspend fun eliminar(entity: RenglonComprobanteEntity) = dao.delete(entity)

    suspend fun eliminarTodo() = dao.clearAll()
}
