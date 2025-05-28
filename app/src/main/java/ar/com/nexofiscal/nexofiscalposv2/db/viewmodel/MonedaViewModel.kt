// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/MonedaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.MonedaRepository

class MonedaViewModel(context: Context) : ViewModel() {

    private val repo = MonedaRepository(
        AppDatabase.getInstance(context).monedaDao()
    )

    /** Lista observable de monedas */
    val monedas = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una moneda */
    fun save(m: MonedaEntity) {
        viewModelScope.launch { repo.guardar(m) }
    }

    /** Elimina una moneda */
    fun delete(m: MonedaEntity) {
        viewModelScope.launch { repo.eliminar(m) }
    }

    /** Carga una moneda por id */
    fun loadById(id: Int, callback: (MonedaEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
