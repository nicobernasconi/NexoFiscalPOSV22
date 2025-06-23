package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoIvaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipoIvaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TipoIvaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tipoIvaDao()
        repo = TipoIvaRepository(dao)
    }

    val pagedTiposIva: Flow<PagingData<TipoIVA>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTiposIvaPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(ti: TipoIvaEntity) {
        viewModelScope.launch {
            if (ti.serverId == null) {
                ti.syncStatus = SyncStatus.CREATED
            } else {
                ti.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(ti)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(ti: TipoIvaEntity) {
        viewModelScope.launch {
            ti.syncStatus = SyncStatus.DELETED
            repo.actualizar(ti)
        }
    }
}