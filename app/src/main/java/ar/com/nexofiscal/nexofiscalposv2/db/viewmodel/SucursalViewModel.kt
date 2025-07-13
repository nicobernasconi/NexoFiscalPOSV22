package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Sucursal
import ar.com.nexofiscal.nexofiscalposv2.db.repository.SucursalRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SucursalViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: SucursalRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).sucursalDao()
        repo = SucursalRepository(dao)
    }

    val pagedSucursales: Flow<PagingData<Sucursal>> = _searchQuery
        .flatMapLatest { query ->
            repo.getSucursalesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(s: SucursalEntity) {
        viewModelScope.launch {
            if (s.serverId == null || s.serverId == 0) {
                s.syncStatus = SyncStatus.CREATED
            } else {
                s.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(s)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(s: SucursalEntity) {
        viewModelScope.launch {
            s.syncStatus = SyncStatus.DELETED
            repo.actualizar(s)
        }
    }
}