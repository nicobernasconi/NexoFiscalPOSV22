// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/LocalidadRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.LocalidadDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity

class LocalidadRepository(private val dao: LocalidadDao) {

    /** Flujo con todas las localidades */
    fun todas(): Flow<List<LocalidadEntity>> = dao.getAll()

    /** Obtiene una localidad por su ID */
    suspend fun porId(id: Int): LocalidadEntity? = dao.getById(id)

    /** Inserta o reemplaza una localidad */
    suspend fun guardar(localidad: LocalidadEntity) = dao.insert(localidad)

    /** Actualiza una localidad existente */
    suspend fun actualizar(localidad: LocalidadEntity) = dao.update(localidad)

    /** Elimina una localidad */
    suspend fun eliminar(localidad: LocalidadEntity) = dao.delete(localidad)

    /** Borra todas las localidades */
    suspend fun eliminarTodo() = dao.clearAll()
}
