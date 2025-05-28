// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/PromocionViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.PromocionRepository

class PromocionViewModel(context: Context) : ViewModel() {

    private val repo = PromocionRepository(
        AppDatabase.getInstance(context).promocionDao()
    )

    /** Lista observable de promociones */
    val promociones = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una promoción */
    fun save(p: PromocionEntity) {
        viewModelScope.launch { repo.guardar(p) }
    }

    /** Elimina una promoción */
    fun delete(p: PromocionEntity) {
        viewModelScope.launch { repo.eliminar(p) }
    }

    /** Carga una promoción por id */
    fun loadById(id: Int, callback: (PromocionEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
