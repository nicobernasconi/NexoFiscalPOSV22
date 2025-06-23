package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import ar.com.nexofiscal.nexofiscalposv2.db.repository.AgrupacionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgrupacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: AgrupacionRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).agrupacionDao()
        repo = AgrupacionRepository(dao)
    }

    val pagedAgrupaciones: Flow<PagingData<Agrupacion>> = _searchQuery
        .flatMapLatest { query ->
            repo.getAgrupacionesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun addOrUpdate(agrup: AgrupacionEntity) {
        viewModelScope.launch {
            if (agrup.serverId == null) { // Si no tiene ID de servidor, es nuevo.
                agrup.syncStatus = SyncStatus.CREATED
            } else { // Si ya tiene ID de servidor, es una modificación.
                agrup.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(agrup)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" que actualiza el estado ---
    fun remove(agrup: AgrupacionEntity) {
        viewModelScope.launch {
            agrup.syncStatus = SyncStatus.DELETED
            repo.actualizar(agrup)
        }
    }
}