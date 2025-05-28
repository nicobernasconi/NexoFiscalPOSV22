// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/SucursalRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SucursalDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity

class SucursalRepository(private val dao: SucursalDao) {

    /** Flujo con todas las sucursales */
    fun todas(): Flow<List<SucursalEntity>> = dao.getAll()

    /** Obtiene una sucursal por su id */
    suspend fun porId(id: Int): SucursalEntity? = dao.getById(id)

    /** Inserta o reemplaza una sucursal */
    suspend fun guardar(suc: SucursalEntity) = dao.insert(suc)

    /** Actualiza una sucursal existente */
    suspend fun actualizar(suc: SucursalEntity) = dao.update(suc)

    /** Elimina una sucursal */
    suspend fun eliminar(suc: SucursalEntity) = dao.delete(suc)

    /** Borra todas las sucursales */
    suspend fun eliminarTodo() = dao.clearAll()
}
