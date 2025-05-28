// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/FormaPagoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.FormaPagoRepository

class FormaPagoViewModel(context: Context) : ViewModel() {

    private val repo = FormaPagoRepository(
        AppDatabase.getInstance(context).formaPagoDao()
    )

    /** Lista reactiva de formas de pago */
    val formasPago = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza */
    fun save(f: FormaPagoEntity) {
        viewModelScope.launch { repo.guardar(f) }
    }

    /** Elimina */
    fun delete(f: FormaPagoEntity) {
        viewModelScope.launch { repo.eliminar(f) }
    }

    /** Carga por id y devuelve por callback */
    fun loadById(id: Int, callback: (FormaPagoEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
