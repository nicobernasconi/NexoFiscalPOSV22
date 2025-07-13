package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.db.repository.PaisRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProvinciaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProvinciaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ProvinciaRepository
    private val paisRepo: PaisRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = ProvinciaRepository(db.provinciaDao())
        paisRepo = PaisRepository(db.paisDao())
    }

    val pagedProvincias: Flow<PagingData<Provincia>> = _searchQuery
        .flatMapLatest { query ->
            // El repo ahora devuelve un Flow<PagingData<ProvinciaConDetalles>>
            repo.getProvinciasPaginated(query)
        }
        .map { pagingData ->
            // El map ahora es simple: solo llama al nuevo mapper síncrono
            pagingData.map { provinciaConDetalles ->
                provinciaConDetalles.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(p: ProvinciaEntity) {
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
    fun delete(p: ProvinciaEntity) {
        viewModelScope.launch {
            p.syncStatus = SyncStatus.DELETED
            repo.actualizar(p)
        }
    }
}