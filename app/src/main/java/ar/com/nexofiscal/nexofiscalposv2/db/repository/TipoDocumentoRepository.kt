// 4. Repositorio
// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/TipoDocumentoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.TipoDocumentoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity

class TipoDocumentoRepository(private val dao: TipoDocumentoDao) {

    /** Flujo con todos los tipos de documento */
    fun todos(): Flow<List<TipoDocumentoEntity>> = dao.getAll()

    /** Obtiene un tipo por su id */
    suspend fun porId(id: Int): TipoDocumentoEntity? = dao.getById(id)

    /** Inserta o reemplaza un tipo */
    suspend fun guardar(item: TipoDocumentoEntity) = dao.insert(item)

    /** Actualiza un tipo existente */
    suspend fun actualizar(item: TipoDocumentoEntity) = dao.update(item)

    /** Elimina un tipo */
    suspend fun eliminar(item: TipoDocumentoEntity) = dao.delete(item)

    /** Borra todos los registros */
    suspend fun eliminarTodo() = dao.clearAll()
}
