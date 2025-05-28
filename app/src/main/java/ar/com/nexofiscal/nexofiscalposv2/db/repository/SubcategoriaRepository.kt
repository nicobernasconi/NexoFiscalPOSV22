// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/SubcategoriaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.SubcategoriaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity

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
    suspend fun eliminar(item: SubcategoriaEntity) = dao.delete(item)

    /** Borra todas las subcategorías */
    suspend fun eliminarTodo() = dao.clearAll()
}
