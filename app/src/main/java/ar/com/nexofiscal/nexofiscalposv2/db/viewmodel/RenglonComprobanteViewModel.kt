package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.db.repository.RenglonComprobanteRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RenglonComprobanteViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).renglonComprobanteDao()
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
    fun addOrUpdate(model: RenglonComprobante, comprobanteLocalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = gson.toJson(model)
            val entity = RenglonComprobanteEntity(
                // El 'id' local del renglón se autogenera, por eso no se especifica.
                comprobanteLocalId = comprobanteLocalId,
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

    /**
     * Carga todos los renglones para un ID de comprobante específico y los devuelve por callback.
     */
    fun loadByComprobanteId(comprobanteId: Int, callback: (List<RenglonComprobanteEntity>) -> Unit) {
        viewModelScope.launch {
            repo.porComprobante(comprobanteId).collect { renglones ->
                callback(renglones)
            }
        }
    }

    /**
     * Nueva función suspendible que obtiene y parsea los renglones para un comprobante.
     * Reemplaza el método antiguo con callback.
     *
     * @param comprobanteId El ID del comprobante.
     * @return Una lista de objetos RenglonComprobante.
     */
    suspend fun getRenglonesByComprobanteId(comprobanteId: Int): List<RenglonComprobante> {
        // Obtenemos el Flow y tomamos el primer (y único) valor que emitirá.
        val renglonesEntity = repo.porComprobante(comprobanteId).first()

        // Parseamos los datos JSON a nuestros modelos de dominio.
        return renglonesEntity.mapNotNull { entity ->
            try {
                gson.fromJson(entity.data, RenglonComprobante::class.java)
            } catch (e: Exception) {
                // Si hay un error de parseo en un renglón, lo ignoramos.
                null
            }
        }
    }
}