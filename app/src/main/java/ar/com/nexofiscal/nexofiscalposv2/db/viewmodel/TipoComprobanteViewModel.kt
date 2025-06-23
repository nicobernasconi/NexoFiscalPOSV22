package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoComprobanteRepository
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipoComprobanteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TipoComprobanteRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tipoComprobanteDao()
        repo = TipoComprobanteRepository(dao)
    }

    val pagedTiposComprobante: Flow<PagingData<TipoComprobante>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTiposComprobantePaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    suspend fun getById(id: Int): TipoComprobante? {
        return repo.porId(id)?.toDomainModel()
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(tc: TipoComprobanteEntity) {
        viewModelScope.launch {
            if (tc.serverId == null) {
                tc.syncStatus = SyncStatus.CREATED
            } else {
                tc.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(tc)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(tc: TipoComprobanteEntity) {
        viewModelScope.launch {
            tc.syncStatus = SyncStatus.DELETED
            repo.actualizar(tc)
        }
    }
}