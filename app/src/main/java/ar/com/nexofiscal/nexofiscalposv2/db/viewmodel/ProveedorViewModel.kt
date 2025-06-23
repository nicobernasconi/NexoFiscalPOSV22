package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CategoriaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.LocalidadRepository
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProveedorRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoIvaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProveedorViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ProveedorRepository
    private val localidadRepo: LocalidadRepository
    private val tipoIvaRepo: TipoIvaRepository
    private val categoriaRepo: CategoriaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = ProveedorRepository(db.proveedorDao())
        localidadRepo = LocalidadRepository(db.localidadDao())
        tipoIvaRepo = TipoIvaRepository(db.tipoIvaDao())
        categoriaRepo = CategoriaRepository(db.categoriaDao())
    }

    val pagedProveedores: Flow<PagingData<Proveedor>> = _searchQuery
        .flatMapLatest { query ->
            repo.getProveedoresPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity ->
                val domainModel = entity.toDomainModel()
                entity.localidadId?.let { domainModel.localidad = localidadRepo.porId(it)?.toDomainModel() }
                entity.tipoIvaId?.let { domainModel.tipoIva = tipoIvaRepo.porId(it)?.toDomainModel() }
                entity.categoriaId?.let { domainModel.categoria = categoriaRepo.porId(it)?.toDomainModel() }
                entity.subcategoriaId?.let { domainModel.subcategoria = categoriaRepo.porId(it)?.toDomainModel() }
                domainModel
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(p: ProveedorEntity) {
        viewModelScope.launch {
            if (p.serverId == null) {
                p.syncStatus = SyncStatus.CREATED
            } else {
                p.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(p)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(p: ProveedorEntity) {
        viewModelScope.launch {
            p.syncStatus = SyncStatus.DELETED
            repo.actualizar(p)
        }
    }
}