// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/CierreCajaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.CierreCajaRepository

class CierreCajaViewModel(context: Context) : ViewModel() {

    private val repo = CierreCajaRepository(
        AppDatabase.getInstance(context).cierreCajaDao()
    )

    /** Lista observable de todos los cierres de caja */
    val cierres = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Agrega o actualiza un cierre */
    fun save(c: CierreCajaEntity) {
        viewModelScope.launch { repo.guardar(c) }
    }

    /** Elimina un cierre */
    fun delete(c: CierreCajaEntity) {
        viewModelScope.launch { repo.eliminar(c) }
    }

    /** Carga un cierre por id y lo pasa al callback */
    fun loadById(id: Int, callback: (CierreCajaEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
