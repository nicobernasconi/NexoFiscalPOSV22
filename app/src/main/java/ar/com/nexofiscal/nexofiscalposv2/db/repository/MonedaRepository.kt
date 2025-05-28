// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/MonedaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.MonedaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity

class MonedaRepository(private val dao: MonedaDao) {

    /** Flujo con todas las monedas */
    fun todas(): Flow<List<MonedaEntity>> = dao.getAll()

    /** Obtiene una moneda por su id */
    suspend fun porId(id: Int): MonedaEntity? = dao.getById(id)

    /** Inserta o reemplaza una moneda */
    suspend fun guardar(m: MonedaEntity) = dao.insert(m)

    /** Actualiza una moneda existente */
    suspend fun actualizar(m: MonedaEntity) = dao.update(m)

    /** Elimina una moneda */
    suspend fun eliminar(m: MonedaEntity) = dao.delete(m)

    /** Borra todas las monedas */
    suspend fun eliminarTodo() = dao.clearAll()
}
