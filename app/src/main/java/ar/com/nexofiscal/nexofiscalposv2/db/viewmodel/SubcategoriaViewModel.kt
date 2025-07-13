package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.repository.SubcategoriaRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubcategoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SubcategoriaRepository(
        AppDatabase.getInstance(application).subcategoriaDao()
    )

    val subcategorias = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(item: SubcategoriaEntity) {
        viewModelScope.launch {
            if (item.serverId == null || item.serverId == 0) {
                item.syncStatus = SyncStatus.CREATED
            } else {
                item.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(item)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(item: SubcategoriaEntity) {
        viewModelScope.launch {
            item.syncStatus = SyncStatus.DELETED
            repo.actualizar(item)
        }
    }

    fun loadById(id: Int, callback: (SubcategoriaEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}