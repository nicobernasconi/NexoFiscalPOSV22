// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ClienteRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ClienteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity

class ClienteRepository(private val dao: ClienteDao) {

    /** Flujo observable con todos los clientes */
    fun todos(): Flow<List<ClienteEntity>> = dao.getAll()

    /** Recupera un cliente por su id */
    suspend fun porId(id: Int): ClienteEntity? = dao.getById(id)

    /** Inserta o actualiza un cliente */
    suspend fun guardar(c: ClienteEntity) = dao.insert(c)

    /** Actualiza un cliente existente */
    suspend fun actualizar(c: ClienteEntity) = dao.update(c)

    /** Elimina un cliente */
    suspend fun eliminar(c: ClienteEntity) = dao.delete(c)

    /** Borra todos los registros de clientes */
    suspend fun eliminarTodo() = dao.clearAll()
}
