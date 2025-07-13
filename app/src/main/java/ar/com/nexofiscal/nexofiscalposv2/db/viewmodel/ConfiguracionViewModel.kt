package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.models.BalanzaConfig
import ar.com.nexofiscal.nexofiscalposv2.models.CodigoBarraConfig
import ar.com.nexofiscal.nexofiscalposv2.models.Configuracion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("nexofiscal_config", Context.MODE_PRIVATE)

    private val _configState = MutableStateFlow(loadConfiguracion())
    val configState = _configState.asStateFlow()

    private fun loadConfiguracion(): Configuracion {
        val tiempoDescarga = prefs.getInt("tiempo_descarga", 60)
        val tiempoSubida = prefs.getInt("tiempo_subida", 15)

        val codigoBarra = CodigoBarraConfig(
            inicio = prefs.getInt("cb_inicio", 2),
            id_long = prefs.getInt("cb_id_long", 5),
            payload_type = prefs.getString("cb_payload_type", "P") ?: "P",
            payload_int = prefs.getInt("cb_payload_int", 5),
            long = prefs.getInt("cb_long", 13)
        )

        val balanza = BalanzaConfig(
            puerto = prefs.getString("balanza_puerto", "COM1") ?: "COM1",
            baudios = prefs.getInt("balanza_baudios", 9600),
            reintentos = prefs.getInt("balanza_reintentos", 3)
        )

        return Configuracion(
            tiempoDescargaMinutos = tiempoDescarga,
            tiempoSubidaMinutos = tiempoSubida,
            codigoBarra = codigoBarra,
            balanza = balanza
        )
    }

    fun saveConfiguracion(config: Configuracion) {
        viewModelScope.launch {
            prefs.edit().apply {
                putInt("tiempo_descarga", config.tiempoDescargaMinutos)
                putInt("tiempo_subida", config.tiempoSubidaMinutos)
                putInt("cb_inicio", config.codigoBarra.inicio)
                putInt("cb_id_long", config.codigoBarra.id_long)
                putString("cb_payload_type", config.codigoBarra.payload_type)
                putInt("cb_payload_int", config.codigoBarra.payload_int)
                putInt("cb_long", config.codigoBarra.long)
                putString("balanza_puerto", config.balanza.puerto)
                putInt("balanza_baudios", config.balanza.baudios)
                putInt("balanza_reintentos", config.balanza.reintentos)
                apply()
            }
            _configState.value = config
        }
    }
}