package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import ar.com.nexofiscal.nexofiscalposv2.db.repository.MonedaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MonedaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: MonedaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).monedaDao()
        repo = MonedaRepository(dao)
    }

    val pagedMonedas: Flow<PagingData<Moneda>> = _searchQuery
        .flatMapLatest { query ->
            repo.getMonedasPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(m: MonedaEntity) {
        viewModelScope.launch {
            if (m.serverId == null || m.serverId == 0) {
                m.syncStatus = SyncStatus.CREATED
            } else {
                m.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(m)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(m: MonedaEntity) {
        viewModelScope.launch {
            m.syncStatus = SyncStatus.DELETED
            repo.actualizar(m)
        }
    }
}