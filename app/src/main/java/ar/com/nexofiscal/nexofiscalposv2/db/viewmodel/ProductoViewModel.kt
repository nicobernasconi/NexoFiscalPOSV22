package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.repository.AgrupacionRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.FamiliaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.MonedaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProductoRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProveedorRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TasaIvaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.TipoRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.UnidadRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _productoParaEditar = MutableStateFlow<Producto?>(null)
    val productoParaEditar: StateFlow<Producto?> = _productoParaEditar.asStateFlow()

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

    fun cargarProductoParaEdicion(productoLocalId: Int) {
        viewModelScope.launch {
            Log.d("EditFlow", "1. ViewModel: Solicitado cargar producto con ID local $productoLocalId")
            val productoConDetalles = repo.getConDetallesById(productoLocalId)
            if (productoConDetalles != null) {
                val productoCompleto = productoConDetalles.toDomainModel()
                _productoParaEditar.value = productoCompleto
                Log.d("EditFlow", "2. ViewModel: Producto cargado y emitido al StateFlow. Familia: ${productoCompleto.familia?.nombre}")
            } else {
                NotificationManager.show("Error: No se pudo cargar el producto para editar.", NotificationType.ERROR)
                _productoParaEditar.value = null
            }
        }
    }

    fun limpiarProductoParaEdicion() {
        _productoParaEditar.value = null
        Log.d("EditFlow", "5. ViewModel: Estado de edición limpiado.")
    }

    suspend fun findByBarcode(barcode: String): Producto? {
        val entity = repo.findByBarcode(barcode) ?: return null
        // Al encontrar por código de barras, también necesitamos cargar los detalles
        val fullDetails = repo.getConDetallesById(entity.id)
        return fullDetails?.toDomainModel()
    }

    val favoritos: StateFlow<List<Producto>> = repo.getFavoritosWithDetails()
        .map { listProductoConDetalles ->
            listProductoConDetalles.map { it.toDomainModel() }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun getProductoConDetallesById(id: Int): Producto? {
        return repo.getConDetallesById(id)?.toDomainModel()
    }

    fun save(p: Producto) { // Recibe el modelo de dominio
        viewModelScope.launch {
            val entity = p.toEntity() // Esta llamada ahora preserva el localId

            // Validación: impedir crear un producto con código duplicado.
            // Si es edición (id/localId != 0), se permite mismo código.
            val isCreate = (entity.id == 0)
            val codigo = entity.codigo?.trim()
            if (isCreate && !codigo.isNullOrEmpty()) {
                val existe = repo.existeCodigo(codigo)
                if (existe) {
                    NotificationManager.show("Ya existe un producto con el código '$codigo'.", NotificationType.ERROR)
                    return@launch
                }
            }

            if (entity.serverId == null || entity.serverId == 0) {
                entity.syncStatus = SyncStatus.CREATED
            } else {
                entity.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(entity)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    fun delete(p: Producto) { // Recibimos el modelo de dominio
        viewModelScope.launch {
            try {
                val entity = p.toEntity()
                repo.eliminar(entity)
                NotificationManager.show("Producto eliminado.", NotificationType.SUCCESS)
            } catch (e: Exception) {
                NotificationManager.show(e.message ?: "No se puede borrar el producto.", NotificationType.ERROR)
            }
        }
    }
}