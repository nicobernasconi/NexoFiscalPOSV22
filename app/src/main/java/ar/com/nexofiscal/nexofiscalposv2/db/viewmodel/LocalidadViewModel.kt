package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.db.repository.LocalidadRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProvinciaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocalidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: LocalidadRepository
    private val provinciaRepo: ProvinciaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val database = AppDatabase.getInstance(application)
        repo = LocalidadRepository(database.localidadDao())
        provinciaRepo = ProvinciaRepository(database.provinciaDao())
    }

    val pagedLocalidades: Flow<PagingData<Localidad>> = _searchQuery
        .flatMapLatest { query ->
            repo.getLocalidadesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity ->
                val domainModel = entity.toDomainModel()
                entity.provinciaId?.let { provId ->
                    val provinciaEntity = provinciaRepo.porId(provId)
                    domainModel.provincia = provinciaEntity?.toDomainModel()
                }
                domainModel
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(l: LocalidadEntity) {
        viewModelScope.launch {
            if (l.serverId == null || l.serverId == 0) {
                l.syncStatus = SyncStatus.CREATED
            } else {
                l.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(l)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(l: LocalidadEntity) {
        viewModelScope.launch {
            l.syncStatus = SyncStatus.DELETED
            repo.actualizar(l)
        }
    }
}