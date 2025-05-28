// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/PromocionRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.PromocionDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity

class PromocionRepository(private val dao: PromocionDao) {

    /** Flujo con todas las promociones */
    fun todas(): Flow<List<PromocionEntity>> = dao.getAll()

    /** Obtiene una promoción por su id */
    suspend fun porId(id: Int): PromocionEntity? = dao.getById(id)

    /** Inserta o reemplaza una promoción */
    suspend fun guardar(p: PromocionEntity) = dao.insert(p)

    /** Actualiza una promoción existente */
    suspend fun actualizar(p: PromocionEntity) = dao.update(p)

    /** Elimina una promoción */
    suspend fun eliminar(p: PromocionEntity) = dao.delete(p)

    /** Borra todas las promociones */
    suspend fun eliminarTodo() = dao.clearAll()
}
