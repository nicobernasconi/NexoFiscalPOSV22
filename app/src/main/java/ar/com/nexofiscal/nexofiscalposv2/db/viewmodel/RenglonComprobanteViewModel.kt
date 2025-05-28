// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/RenglonComprobanteViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.repository.RenglonComprobanteRepository

class RenglonComprobanteViewModel(context: Context) : ViewModel() {

    private val dao = AppDatabase.getInstance(context).renglonComprobanteDao()
    private val repo = RenglonComprobanteRepository(dao)
    private val gson = Gson()

    /** Flow de todas las líneas */
    val lineas = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Flow de líneas de un comprobante
     */
    fun lineasDe(comprobanteId: Int) = repo.porComprobante(comprobanteId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Insertar o actualizar un RenglonComprobante */
    fun addOrUpdate(model: RenglonComprobante) {
        viewModelScope.launch {
            val json = gson.toJson(model)
            val entity = RenglonComprobanteEntity(
                id = model.id,
                comprobanteId = model.comprobanteId,
                data = json
            )
            repo.guardar(entity)
        }
    }

    /** Eliminar uno */
    fun remove(model: RenglonComprobante) {
        viewModelScope.launch {
            repo.eliminar(RenglonComprobanteEntity(model.id, model.comprobanteId, data = ""))
        }
    }

    /** Cargar uno y deserializarlo */
    fun loadById(id: Int, callback: (RenglonComprobante?)->Unit) {
        viewModelScope.launch {
            val entity = repo.porId(id)
            val model = entity?.let { gson.fromJson(it.data, RenglonComprobante::class.java) }
            callback(model)
        }
    }
}
