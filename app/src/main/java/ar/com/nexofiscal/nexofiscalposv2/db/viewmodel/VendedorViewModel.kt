package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import ar.com.nexofiscal.nexofiscalposv2.db.repository.VendedorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VendedorViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: VendedorRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).vendedorDao()
        repo = VendedorRepository(dao)
    }

    val pagedVendedores: Flow<PagingData<Vendedor>> = _searchQuery
        .flatMapLatest { query ->
            repo.getVendedoresPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(v: VendedorEntity) {
        viewModelScope.launch {
            if (v.serverId == null) {
                v.syncStatus = SyncStatus.CREATED
            } else {
                v.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(v)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(v: VendedorEntity) {
        viewModelScope.launch {
            v.syncStatus = SyncStatus.DELETED
            repo.actualizar(v)
        }
    }
}