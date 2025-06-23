package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.repository.StockProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StockProductoRepository(
        AppDatabase.getInstance(application).stockProductoDao()
    )

    val stockProductos = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- CAMBIO: Lógica de guardado ahora establece el estado de sincronización ---
    fun save(item: StockProductoEntity) {
        viewModelScope.launch {
            if (item.serverId == null) {
                item.syncStatus = SyncStatus.CREATED
            } else {
                item.syncStatus = SyncStatus.UPDATED
            }
            repo.guardar(item)
        }
    }

    // --- CAMBIO: El borrado ahora es un "soft delete" ---
    fun delete(item: StockProductoEntity) {
        viewModelScope.launch {
            item.syncStatus = SyncStatus.DELETED
            repo.actualizar(item)
        }
    }

    fun loadById(id: Int, callback: (StockProductoEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}