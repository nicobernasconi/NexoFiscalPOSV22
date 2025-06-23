package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Rol
import ar.com.nexofiscal.nexofiscalposv2.db.repository.RolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RolViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: RolRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).rolDao()
        repo = RolRepository(dao)
    }

    val pagedRoles: Flow<PagingData<Rol>> = _searchQuery
        .flatMapLatest { query ->
            repo.getRolesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(r: RolEntity) {
        viewModelScope.launch {
            if (r.serverId == null) {
                r.syncStatus = SyncStatus.CREATED
            } else {
                r.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(r)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(r: RolEntity) {
        viewModelScope.launch {
            r.syncStatus = SyncStatus.DELETED
            repo.actualizar(r)
        }
    }
}