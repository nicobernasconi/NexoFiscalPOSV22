// src/main/java/ar/com/nexofiscal/nexofiscalposv2/repository/CierreCajaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.repository

import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.dao.CierreCajaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity

class CierreCajaRepository(private val dao: CierreCajaDao) {

    /** Devuelve todos los cierres como Flow para observar en tiempo real */
    fun todos(): Flow<List<CierreCajaEntity>> = dao.getAll()

    /** Obtiene un cierre en particular por su id */
    suspend fun porId(id: Int): CierreCajaEntity? = dao.getById(id)

    /** Inserta o reemplaza un cierre */
    suspend fun guardar(cierre: CierreCajaEntity) = dao.insert(cierre)

    /** Actualiza un cierre existente */
    suspend fun actualizar(cierre: CierreCajaEntity) = dao.update(cierre)

    /** Elimina un cierre */
    suspend fun eliminar(cierre: CierreCajaEntity) = dao.delete(cierre)

    /** Borra todos los registros de cierres */
    suspend fun eliminarTodo() = dao.clearAll()
}
