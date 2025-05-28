// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/FormaPagoRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.FormaPagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity

class FormaPagoRepository(private val dao: FormaPagoDao) {

    /** Flujo con todas las formas de pago */
    fun todas(): Flow<List<FormaPagoEntity>> = dao.getAll()

    /** Obtiene una forma de pago por su id */
    suspend fun porId(id: Int): FormaPagoEntity? = dao.getById(id)

    /** Inserta o reemplaza */
    suspend fun guardar(f: FormaPagoEntity) = dao.insert(f)

    /** Actualiza existente */
    suspend fun actualizar(f: FormaPagoEntity) = dao.update(f)

    /** Elimina */
    suspend fun eliminar(f: FormaPagoEntity) = dao.delete(f)

    /** Borra todas las formas de pago */
    suspend fun eliminarTodo() = dao.clearAll()
}
