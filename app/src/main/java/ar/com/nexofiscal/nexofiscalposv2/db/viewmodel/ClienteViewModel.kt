// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/ClienteViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.ClienteRepository

class ClienteViewModel(context: Context) : ViewModel() {

    private val repo = ClienteRepository(
        AppDatabase.getInstance(context).clienteDao()
    )

    /** Lista reactiva de clientes */
    val clientes = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda (inserta o reemplaza) un cliente */
    fun save(c: ClienteEntity) {
        viewModelScope.launch { repo.guardar(c) }
    }

    /** Elimina un cliente */
    fun delete(c: ClienteEntity) {
        viewModelScope.launch { repo.eliminar(c) }
    }

    /** Carga un cliente por id */
    fun loadById(id: Int, callback: (ClienteEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
