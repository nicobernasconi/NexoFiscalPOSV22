package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.*
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ProductoRepository
    private val monedaRepo: MonedaRepository
    private val tasaIvaRepo: TasaIvaRepository
    private val familiaRepo: FamiliaRepository
    private val agrupacionRepo: AgrupacionRepository
    private val proveedorRepo: ProveedorRepository
    private val tipoRepo: TipoRepository
    private val unidadRepo: UnidadRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = ProductoRepository(db.productoDao())
        monedaRepo = MonedaRepository(db.monedaDao())
        tasaIvaRepo = TasaIvaRepository(db.tasaIvaDao())
        familiaRepo = FamiliaRepository(db.familiaDao())
        agrupacionRepo = AgrupacionRepository(db.agrupacionDao())
        proveedorRepo = ProveedorRepository(db.proveedorDao())
        tipoRepo = TipoRepository(db.tipoDao())
        unidadRepo = UnidadRepository(db.unidadDao())
    }

    val pagedProductos: Flow<PagingData<Producto>> = _searchQuery
        .flatMapLatest { query ->
            repo.getProductosPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { productoConDetalles ->
                productoConDetalles.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    suspend fun findByBarcode(barcode: String): Producto? {
        val entity = repo.findByBarcode(barcode) ?: return null
        val domainModel = entity.toDomainModel()
        // ... (lógica de enriquecimiento del modelo)
        return domainModel
    }

    val favoritos: StateFlow<List<Producto>> = repo.getFavoritosWithDetails()
        .map { listProductoConDetalles ->
            listProductoConDetalles.map { it.toDomainModel() }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun getProductoConDetallesById(id: Int): Producto? {
        return repo.getConDetallesById(id)?.toDomainModel()
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(p: ProductoEntity) {
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
    fun delete(p: ProductoEntity) {
        viewModelScope.launch {
            p.syncStatus = SyncStatus.DELETED
            repo.actualizar(p)
        }
    }
}