// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/UsuarioRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import ar.com.nexofiscal.nexofiscalposv2.db.dao.UsuarioDao
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity

class UsuarioRepository(private val dao: UsuarioDao) {

    /** Flujo con todos los usuarios */
    fun todos(): Flow<List<UsuarioEntity>> = dao.getAll()

    /** Obtiene un usuario por id */
    suspend fun porId(id: Int): UsuarioEntity? = dao.getById(id)

    /** Inserta o actualiza */
    suspend fun guardar(usuario: UsuarioEntity) = dao.insert(usuario)

    /** Actualiza existente */
    suspend fun actualizar(usuario: UsuarioEntity) = dao.update(usuario)

    /** Elimina */
    suspend fun eliminar(usuario: UsuarioEntity) = dao.delete(usuario)

    /** Borra todos */
    suspend fun eliminarTodo() = dao.clearAll()
}
