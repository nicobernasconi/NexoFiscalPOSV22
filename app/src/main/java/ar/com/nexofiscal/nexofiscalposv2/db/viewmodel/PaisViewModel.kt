package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.db.repository.PaisRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaisViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PaisRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).paisDao()
        repo = PaisRepository(dao)
    }

    val pagedPaises: Flow<PagingData<Pais>> = _searchQuery
        .flatMapLatest { query ->
            repo.getPaisesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(p: PaisEntity) {
        viewModelScope.launch {
            if (p.serverId == null) {
                p.syncStatus = SyncStatus.CREATED
            } else {
                p.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(p)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(p: PaisEntity) {
        viewModelScope.launch {
            p.syncStatus = SyncStatus.DELETED
            repo.actualizar(p)
        }
    }
}