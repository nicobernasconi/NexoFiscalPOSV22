package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.db.repository.PromocionRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PromocionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PromocionRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).promocionDao()
        repo = PromocionRepository(dao)
    }

    val pagedPromociones: Flow<PagingData<Promocion>> = _searchQuery
        .flatMapLatest { query ->
            repo.getPromocionesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(p: PromocionEntity) {
        viewModelScope.launch {
            if (p.serverId == null || p.serverId == 0) {
                p.syncStatus = SyncStatus.CREATED
            } else {
                p.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(p)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(p: PromocionEntity) {
        viewModelScope.launch {
            p.syncStatus = SyncStatus.DELETED
            repo.actualizar(p)
        }
    }
}