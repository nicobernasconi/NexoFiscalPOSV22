package ar.com.nexofiscal.nexofiscalposv2.db.repository

import ar.com.nexofiscal.nexofiscalposv2.db.dao.RolDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity
import kotlinx.coroutines.flow.Flow

class RolRepository(private val dao: RolDao) {

    /** Flujo con todos los roles */
    fun todos(): Flow<List<RolEntity>> = dao.getAll()

    /** Obtiene un rol por su id */
    suspend fun porId(id: Int): RolEntity? = dao.getById(id)

    /** Inserta o reemplaza un rol */
    suspend fun guardar(r: RolEntity) = dao.insert(r)

    /** Actualiza un rol existente */
    suspend fun actualizar(r: RolEntity) = dao.update(r)

    /** Elimina un rol */
    suspend fun eliminar(r: RolEntity) = dao.delete(r)

    /** Borra todos los roles */
    suspend fun eliminarTodo() = dao.clearAll()
}