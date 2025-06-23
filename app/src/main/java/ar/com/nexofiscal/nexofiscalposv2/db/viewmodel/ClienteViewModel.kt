package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ClienteRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = AppDatabase.getInstance(application).clienteDao()
        repo = ClienteRepository(dao)
    }

    val pagedClientes: Flow<PagingData<Cliente>> = _searchQuery
        .flatMapLatest { query ->
            repo.getClientesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { clienteEntity ->
                clienteEntity.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(c: ClienteEntity) {
        viewModelScope.launch {
            if (c.serverId == null) {
                c.syncStatus = SyncStatus.CREATED
            } else {
                c.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(c)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(c: ClienteEntity) {
        viewModelScope.launch {
            c.syncStatus = SyncStatus.DELETED
            repo.actualizar(c)
        }
    }

    suspend fun getById(id: Int): ClienteEntity? {
        return repo.porId(id)
    }
}