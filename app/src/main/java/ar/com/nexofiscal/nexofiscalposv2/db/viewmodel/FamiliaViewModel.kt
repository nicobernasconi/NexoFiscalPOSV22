package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Familia
import ar.com.nexofiscal.nexofiscalposv2.db.repository.FamiliaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FamiliaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: FamiliaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).familiaDao()
        repo = FamiliaRepository(dao)
    }

    val pagedFamilias: Flow<PagingData<Familia>> = _searchQuery
        .flatMapLatest { query ->
            repo.getFamiliasPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    suspend fun porId(id: Int): FamiliaEntity? = repo.porId(id)

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun guardar(entity: FamiliaEntity) {
        viewModelScope.launch {
            if (entity.serverId == null || entity.serverId == 0) {
                entity.syncStatus = SyncStatus.CREATED
            } else {
                entity.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(entity)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora valida dependencias y notifica ---
    fun eliminar(entity: FamiliaEntity) {
        viewModelScope.launch {
            try {
                repo.eliminar(entity)
                NotificationManager.show("Familia eliminada.", NotificationType.SUCCESS)
            } catch (e: Exception) {
                NotificationManager.show(e.message ?: "No se puede borrar la familia.", NotificationType.ERROR)
            }
        }
    }

    suspend fun eliminarTodo() = repo.eliminarTodo()
}