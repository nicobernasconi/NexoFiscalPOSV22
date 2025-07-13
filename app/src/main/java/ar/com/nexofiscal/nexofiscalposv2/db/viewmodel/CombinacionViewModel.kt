package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CombinacionRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CombinacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CombinacionRepository(
        AppDatabase.getInstance(application).combinacionDao()
    )

    val combinaciones = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(c: CombinacionEntity) {
        viewModelScope.launch {
            if (c.uid == 0) { // Si el uid local es 0, es un registro nuevo
                c.syncStatus = SyncStatus.CREATED
            } else { // Si no, es una modificación
                c.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(c)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    // --- CAMBIO: El borrado es un "soft delete" que actualiza el estado ---
    fun delete(c: CombinacionEntity) {
        viewModelScope.launch {
            c.syncStatus = SyncStatus.DELETED
            repo.actualizar(c)
        }
    }

    fun loadByUid(uid: Int, callback: (CombinacionEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porUid(uid))
        }
    }
}