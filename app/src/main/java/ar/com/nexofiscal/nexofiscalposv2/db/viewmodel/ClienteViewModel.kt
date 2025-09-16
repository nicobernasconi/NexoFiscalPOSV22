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
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.db.repository.*
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ClienteRepository
    private val tipoDocumentoRepo: TipoDocumentoRepository
    private val tipoIvaRepo: TipoIvaRepository
    private val localidadRepo: LocalidadRepository
    private val categoriaRepo: CategoriaRepository
    private val vendedorRepo: VendedorRepository
    private val provinciaRepo: ProvinciaRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = ClienteRepository(db.clienteDao())
        tipoDocumentoRepo = TipoDocumentoRepository(db.tipoDocumentoDao())
        tipoIvaRepo = TipoIvaRepository(db.tipoIvaDao())
        localidadRepo = LocalidadRepository(db.localidadDao())
        categoriaRepo = CategoriaRepository(db.categoriaDao())
        vendedorRepo = VendedorRepository(db.vendedorDao())
        provinciaRepo = ProvinciaRepository(db.provinciaDao())
    }

    private val _clienteParaEditar = MutableStateFlow<Cliente?>(null)
    val clienteParaEditar: StateFlow<Cliente?> = _clienteParaEditar.asStateFlow()

    // El Paging ahora recibe ClienteConDetalles y necesita mapearlo
    val pagedClientes: Flow<PagingData<Cliente>> = _searchQuery
        .flatMapLatest { query ->
            repo.getClientesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { clienteConDetalles ->
                // El mapper se encargará de convertir la entidad compuesta al modelo de dominio
                clienteConDetalles.toDomainModel()
            }
        }
        .cachedIn(viewModelScope)

    fun cargarClienteParaEdicion(clienteLocalId: Int) {
        viewModelScope.launch {
            val clienteConDetalles = repo.getConDetallesById(clienteLocalId)
            if (clienteConDetalles != null) {
                _clienteParaEditar.value = clienteConDetalles.toDomainModel()
            } else {
                NotificationManager.show("Error: No se pudo cargar el cliente para editar.", NotificationType.ERROR)
                _clienteParaEditar.value = null
            }
        }
    }

    fun limpiarClienteParaEdicion() {
        _clienteParaEditar.value = null
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun save(cliente: Cliente) { // Recibe el modelo de dominio
        viewModelScope.launch {
            // VALIDACIÓN: impedir guardar cliente con mismo CUIT que la empresa
            val empresaCuit = SessionManager.empresaCuit?.replace("-", "")?.trim()
            val clienteCuit = cliente.cuit?.replace("-", "")?.trim()
            if (!empresaCuit.isNullOrBlank() && !clienteCuit.isNullOrBlank() && empresaCuit == clienteCuit) {
                NotificationManager.show("No se puede registrar un cliente con el mismo CUIT de la empresa.", NotificationType.ERROR)
                return@launch
            }
            val entity = cliente.toEntity() // Esta llamada ahora preserva el localId
            if (entity.serverId == null || entity.serverId == 0) {
                entity.syncStatus = SyncStatus.CREATED
            } else {
                entity.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(entity) // Room hará un UPDATE porque el ID ya existe
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    fun delete(c: ClienteEntity) {
        viewModelScope.launch {
            try {
                repo.eliminar(c)
                NotificationManager.show("Cliente eliminado.", NotificationType.SUCCESS)
            } catch (e: Exception) {
                NotificationManager.show(e.message ?: "No se puede borrar el cliente.", NotificationType.ERROR)
            }
        }
    }

    suspend fun getById(id: Int): ClienteEntity? {
        return repo.porId(id)
    }
}
