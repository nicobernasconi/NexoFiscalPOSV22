package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TasaIvaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasaIvaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TasaIvaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tasaIvaDao()
        repo = TasaIvaRepository(dao)
    }

    val pagedTasasIva: Flow<PagingData<TasaIva>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTasasIvaPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(t: TasaIvaEntity) {
        viewModelScope.launch {
            if (t.serverId == null || t.serverId == 0) {
                t.syncStatus = SyncStatus.CREATED
            } else {
                t.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(t)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora valida dependencias y notifica ---
    fun delete(t: TasaIvaEntity) {
        viewModelScope.launch {
            try {
                repo.eliminar(t)
                NotificationManager.show("Tasa de IVA eliminada.", NotificationType.SUCCESS)
            } catch (e: Exception) {
                NotificationManager.show(e.message ?: "No se puede borrar la tasa de IVA.", NotificationType.ERROR)
            }
        }
    }
}