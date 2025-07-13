package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.db.repository.FormaPagoRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoFormaPagoRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FormaPagoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: FormaPagoRepository
    private val tipoFormaPagoRepo: TipoFormaPagoRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val database = AppDatabase.getInstance(application)
        repo = FormaPagoRepository(database.formaPagoDao())
        tipoFormaPagoRepo = TipoFormaPagoRepository(database.tipoFormaPagoDao())
    }

    val allFormasPago: StateFlow<List<FormaPago>> = repo.getAll()
        .map { entities ->
            entities.map { entity ->
                val domainModel = entity.toDomainModel()
                entity.tipoFormaPagoId?.let { tipoId ->
                    domainModel.tipoFormaPago = tipoFormaPagoRepo.porId(tipoId)?.toDomainModel()
                }
                domainModel
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pagedFormasPago: Flow<PagingData<FormaPago>> = _searchQuery
        .flatMapLatest { query ->
            repo.getFormasPagoPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity ->
                val domainModel = entity.toDomainModel()
                entity.tipoFormaPagoId?.let { tipoId ->
                    val tipoEntity = tipoFormaPagoRepo.porId(tipoId)
                    domainModel.tipoFormaPago = tipoEntity?.toDomainModel()
                }
                domainModel
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun guardar(f: FormaPagoEntity) {
        viewModelScope.launch {
            if (f.serverId == null || f.serverId == 0) {
                f.syncStatus = SyncStatus.CREATED
            } else {
                f.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(f)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun eliminar(f: FormaPagoEntity) {
        viewModelScope.launch {
            f.syncStatus = SyncStatus.DELETED
            repo.actualizar(f)
        }
    }

    suspend fun getFirstByTipoId(tipoId: Int): FormaPago? {
        // Llama al repositorio (que a su vez llama al DAO) y mapea el resultado al modelo de dominio.
        return repo.getFirstByTipoId(tipoId)?.toDomainModel()
    }

    suspend fun getAllFormasDePagoCompletas(): List<FormaPago> {
        return repo.getAllWithDetails().map { it.toDomainModel() }
    }
}