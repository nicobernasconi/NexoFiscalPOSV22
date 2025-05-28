// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/ProveedorRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProveedorDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity

class ProveedorRepository(private val dao: ProveedorDao) {

    /** Flujo con todos los proveedores */
    fun todos(): Flow<List<ProveedorEntity>> = dao.getAll()

    /** Obtiene un proveedor por su id */
    suspend fun porId(id: Int): ProveedorEntity? = dao.getById(id)

    /** Inserta o reemplaza un proveedor */
    suspend fun guardar(p: ProveedorEntity) = dao.insert(p)

    /** Actualiza un proveedor existente */
    suspend fun actualizar(p: ProveedorEntity) = dao.update(p)

    /** Elimina un proveedor */
    suspend fun eliminar(p: ProveedorEntity) = dao.delete(p)

    /** Borra todos los proveedores */
    suspend fun eliminarTodo() = dao.clearAll()
}
