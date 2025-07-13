// main/java/ar/com/nexofiscal/nexofiscalposv2/db/repository/UsuarioRepository.kt
package ar.com.nexofiscal.nexofiscalposv2.db.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ar.com.nexofiscal.nexofiscalposv2.db.dao.UsuarioDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioConDetalles // CAMBIO: Importar la clase correcta
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao) {

    // CAMBIO: Se ajusta el tipo de retorno a UsuarioConDetalles
    fun getUsuariosPaginated(query: String): Flow<PagingData<UsuarioConDetalles>> {
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

    suspend fun porId(id: Int): UsuarioEntity? = dao.getById(id)

    suspend fun getConDetallesById(id: Int): UsuarioConDetalles? = dao.getConDetallesById(id)

    suspend fun guardar(usuario: UsuarioEntity) = dao.insert(usuario)

    suspend fun actualizar(usuario: UsuarioEntity) = dao.update(usuario)

    // El borrado físico ya no se usa, el ViewModel se encarga del borrado lógico
    // suspend fun eliminar(usuario: UsuarioEntity) = dao.delete(usuario)



    suspend fun eliminar(entity: UsuarioEntity) {
        entity.syncStatus = SyncStatus.DELETED
        dao.update(entity) // Se utiliza el método update del DAO.
    }
    suspend fun eliminarTodo() = dao.clearAll()
}