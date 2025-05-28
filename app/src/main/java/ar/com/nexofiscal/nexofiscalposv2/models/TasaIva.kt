package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la entidad Tasa de IVA.
 */
class TasaIva {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("tasa")
    var tasa: Double = 0.0
}
