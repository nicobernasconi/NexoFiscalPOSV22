// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TasaIvaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TasaIvaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity

class TasaIvaRepository(private val dao: TasaIvaDao) {

    /** Flujo con todas las tasas de IVA */
    fun todas(): Flow<List<TasaIvaEntity>> = dao.getAll()

    /** Obtiene una tasa por su id */
    suspend fun porId(id: Int): TasaIvaEntity? = dao.getById(id)

    /** Inserta o reemplaza una tasa */
    suspend fun guardar(item: TasaIvaEntity) = dao.insert(item)

    /** Actualiza una tasa existente */
    suspend fun actualizar(item: TasaIvaEntity) = dao.update(item)

    /** Elimina una tasa */
    suspend fun eliminar(item: TasaIvaEntity) = dao.delete(item)

    /** Borra todas las tasas */
    suspend fun eliminarTodo() = dao.clearAll()
}
