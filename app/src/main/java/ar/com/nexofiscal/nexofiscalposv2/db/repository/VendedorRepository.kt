// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/VendedorRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.VendedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity

class VendedorRepository(private val dao: VendedorDao) {

    /** Flujo con todos los vendedores */
    fun todos(): Flow<List<VendedorEntity>> = dao.getAll()

    /** Obtiene un vendedor por id */
    suspend fun porId(id: Int): VendedorEntity? = dao.getById(id)

    /** Inserta o actualiza */
    suspend fun guardar(item: VendedorEntity) = dao.insert(item)

    /** Actualiza existente */
    suspend fun actualizar(item: VendedorEntity) = dao.update(item)

    /** Elimina */
    suspend fun eliminar(item: VendedorEntity) = dao.delete(item)

    /** Borra todos */
    suspend fun eliminarTodo() = dao.clearAll()
}
