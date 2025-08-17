package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Tipo
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TipoRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).tipoDao()
        repo = TipoRepository(dao)
    }

    val pagedTipos: Flow<PagingData<Tipo>> = _searchQuery
        .flatMapLatest { query ->
            repo.getTiposPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(t: TipoEntity) {
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
    fun delete(t: TipoEntity) {
        viewModelScope.launch {
            try {
                repo.eliminar(t)
                NotificationManager.show("Tipo eliminado.", NotificationType.SUCCESS)
            } catch (e: Exception) {
                NotificationManager.show(e.message ?: "No se puede borrar el tipo.", NotificationType.ERROR)
            }
        }
    }
}