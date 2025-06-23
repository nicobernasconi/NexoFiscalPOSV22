// main/java/ar/com/nexofiscal/nexofiscalposv2/db/repository/ProvinciaRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProvinciaDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaConDetalles // CAMBIO: Importar la clase correcta
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity
import kotlinx.coroutines.flow.Flow

class ProvinciaRepository(private val dao: ProvinciaDao) {

    // CAMBIO: Se ajusta el tipo de retorno para que coincida con lo que devuelve el DAO.
    fun getProvinciasPaginated(query: String): Flow<PagingData<ProvinciaConDetalles>> {
        val normalizedQuery = "%${query.trim()}%"
        return Pager(
            config = PagingConfig(pageSize = 200, enablePlaceholders = false),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    dao.getPagingSource()
                } else {
                    dao.searchPagingSource(normalizedQuery)
                }
            }
        ).flow
    }

    suspend fun porId(id: Int): ProvinciaEntity? = dao.getById(id)

    // CAMBIO: Ajuste en los métodos de escritura para usar la entidad base.
    suspend fun guardar(p: ProvinciaEntity) = dao.insert(p)
    suspend fun actualizar(p: ProvinciaEntity) = dao.update(p)

    // Este método ya no es compatible con el borrado lógico, se puede eliminar o adaptar.
    // suspend fun eliminar(p: ProvinciaEntity) = dao.delete(p)

    suspend fun eliminarTodo() = dao.clearAll()
}