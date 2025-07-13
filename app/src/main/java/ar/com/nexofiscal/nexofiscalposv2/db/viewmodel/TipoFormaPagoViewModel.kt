package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoFormaPagoRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipoFormaPagoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TipoFormaPagoRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tipoFormaPagoDao()
        repo = TipoFormaPagoRepository(dao)
    }

    val pagedTiposFormaPago: Flow<PagingData<TipoFormaPago>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTiposFormaPagoPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(tfp: TipoFormaPagoEntity) {
        viewModelScope.launch {
            if (tfp.serverId == null) {
                tfp.syncStatus = SyncStatus.CREATED
            } else {
                tfp.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(tfp)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(tfp: TipoFormaPagoEntity) {
        viewModelScope.launch {
            tfp.syncStatus = SyncStatus.DELETED
            repo.actualizar(tfp)
        }
    }
}