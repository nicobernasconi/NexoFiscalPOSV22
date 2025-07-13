package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoDocumentoRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.TipoDocumento
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipoDocumentoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TipoDocumentoRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tipoDocumentoDao()
        repo = TipoDocumentoRepository(dao)
    }

    val pagedTiposDocumento: Flow<PagingData<TipoDocumento>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTiposDocumentoPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity ->
                // Este mapeo es esencial para convertir la entidad de la BD
                // al objeto que la UI entiende.
                entity.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(td: TipoDocumentoEntity) {
        viewModelScope.launch {
            if (td.serverId == null || td.serverId == 0) {
                td.syncStatus = SyncStatus.CREATED
            } else {
                td.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(td)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(td: TipoDocumentoEntity) {
        viewModelScope.launch {
            td.syncStatus = SyncStatus.DELETED
            repo.actualizar(td)
        }
    }
}