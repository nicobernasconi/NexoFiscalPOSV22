// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TipoFormaPagoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TipoFormaPagoRepository

class TipoFormaPagoViewModel(context: Context) : ViewModel() {

    private val repo = TipoFormaPagoRepository(
        AppDatabase.getInstance(context).tipoFormaPagoDao()
    )

    /** Lista observable de tipos de forma de pago */
    val formasPago = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza */
    fun save(item: TipoFormaPagoEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina */
    fun delete(item: TipoFormaPagoEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga por id */
    fun loadById(id: Int, callback: (TipoFormaPagoEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
