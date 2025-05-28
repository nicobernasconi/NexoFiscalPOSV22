// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/SucursalViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.SucursalRepository

class SucursalViewModel(context: Context) : ViewModel() {

    private val repo = SucursalRepository(
        AppDatabase.getInstance(context).sucursalDao()
    )

    /** Lista observable de sucursales */
    val sucursales = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una sucursal */
    fun save(sucursal: SucursalEntity) {
        viewModelScope.launch { repo.guardar(sucursal) }
    }

    /** Elimina una sucursal */
    fun delete(sucursal: SucursalEntity) {
        viewModelScope.launch { repo.eliminar(sucursal) }
    }

    /** Carga una sucursal por id */
    fun loadById(id: Int, callback: (SucursalEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
