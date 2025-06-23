// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/SubcategoriaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SubcategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

class SubcategoriaRepository(private val dao: SubcategoriaDao) {

    /** Flujo con todas las subcategorías */
    fun todas(): Flow<List<SubcategoriaEntity>> = dao.getAll()

    /** Obtiene una subcategoría por su id */
    suspend fun porId(id: Int): SubcategoriaEntity? = dao.getById(id)

    /** Inserta o reemplaza una subcategoría */
    suspend fun guardar(item: SubcategoriaEntity) = dao.insert(item)

    /** Actualiza una subcategoría existente */
    suspend fun actualizar(item: SubcategoriaEntity) = dao.update(item)

    /** Elimina una subcategoría */
    suspend fun eliminar(entity: SubcategoriaEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }

    /** Borra todas las subcategorías */
    suspend fun eliminarTodo() = dao.clearAll()
}
