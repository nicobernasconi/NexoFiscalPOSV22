package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CategoriaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: CategoriaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).categoriaDao()
        repo = CategoriaRepository(dao)
    }

    val pagedCategorias: Flow<PagingData<Categoria>> = _searchQuery
        .flatMapLatest { query ->
            repo.getCategoriasPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun addOrUpdate(cat: CategoriaEntity) {
        viewModelScope.launch {
            if (cat.serverId == null || cat.serverId == 0) {
                cat.syncStatus = SyncStatus.CREATED
            } else { // Si ya tiene ID de servidor, es una modificación.
                cat.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(cat)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" que actualiza el estado ---
    fun remove(cat: CategoriaEntity) {
        viewModelScope.launch {
            cat.syncStatus = SyncStatus.DELETED
            repo.actualizar(cat) // Se actualiza para marcarla como borrada
        }
    }
}