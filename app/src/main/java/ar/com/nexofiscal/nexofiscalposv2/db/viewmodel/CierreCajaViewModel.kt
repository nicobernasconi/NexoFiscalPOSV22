package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CierreCajaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.UsuarioRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCaja
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CierreCajaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: CierreCajaRepository
    private val usuarioRepo: UsuarioRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getInstance(application)
        repo = CierreCajaRepository(db.cierreCajaDao())
        usuarioRepo = UsuarioRepository(db.usuarioDao())
    }

    val pagedCierresCaja: Flow<PagingData<CierreCaja>> = _searchQuery
        .flatMapLatest { query ->
            repo.getCierresCajaPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { entity ->
                val domainModel = entity.toDomainModel()
                entity.usuarioId?.let { userId ->
                    val userEntity = usuarioRepo.porId(userId)
                    domainModel.usuario = userEntity?.toDomainModel()
                }
                domainModel
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(cierre: CierreCajaEntity) {
        viewModelScope.launch {
            if (cierre.serverId == null || cierre.serverId == 0) {
                cierre.syncStatus = SyncStatus.CREATED
            } else {
                cierre.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(cierre)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(cierre: CierreCajaEntity) {
        viewModelScope.launch {
            cierre.syncStatus = SyncStatus.DELETED
            repo.actualizar(cierre)
        }
    }
}