package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.RolRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.SucursalRepository
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.db.repository.UsuarioRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.VendedorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: UsuarioRepository
    private val rolRepo: RolRepository
    private val sucursalRepo: SucursalRepository
    private val vendedorRepo: VendedorRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = UsuarioRepository(db.usuarioDao())
        rolRepo = RolRepository(db.rolDao())
        sucursalRepo = SucursalRepository(db.sucursalDao())
        vendedorRepo = VendedorRepository(db.vendedorDao())
    }

    val pagedUsuarios: Flow<PagingData<Usuario>> = _searchQuery
        .flatMapLatest { query ->
            // Esta función ahora devuelve un Flow<PagingData<UsuarioConDetalles>>
            repo.getUsuariosPaginated(query)
        }
        .map { pagingData ->
            // "pagingData" es de tipo PagingData<UsuarioConDetalles>
            // Mapeamos cada item de "UsuarioConDetalles" al modelo de dominio "Usuario"
            pagingData.map { usuarioConDetalles ->
                // CAMBIO: Simplemente llamamos al mapper.
                // Este se encarga de convertir la entidad y todas sus relaciones al modelo de dominio.
                usuarioConDetalles.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(u: UsuarioEntity) {
        viewModelScope.launch {
            if (u.serverId == null) {
                u.syncStatus = SyncStatus.CREATED
            } else {
                u.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(u)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(u: UsuarioEntity) {
        viewModelScope.launch {
            u.syncStatus = SyncStatus.DELETED
            repo.actualizar(u)
        }
    }
}