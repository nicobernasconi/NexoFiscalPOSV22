package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.UnidadRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Unidad
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UnidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: UnidadRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).unidadDao()
        repo = UnidadRepository(dao)
    }

    val pagedUnidades: Flow<PagingData<Unidad>> = _searchQuery
        .flatMapLatest { query ->
            repo.getUnidadesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(u: UnidadEntity) {
        viewModelScope.launch {
            if (u.serverId == null || u.serverId == 0) {
                u.syncStatus = SyncStatus.CREATED
            } else {
                u.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(u)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(u: UnidadEntity) {
        viewModelScope.launch {
            u.syncStatus = SyncStatus.DELETED
            repo.actualizar(u)
        }
    }
}