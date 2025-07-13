package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Moneda {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("simbolo")
    var simbolo: String? = null
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("cotizacion")
    var cotizacion: Double = 0.0

    override fun toString(): String {
        return "Moneda(simbolo='$simbolo', nombre='$nombre')"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int = this.id,
        simbolo: String? = this.simbolo,
        nombre: String? = this.nombre,
        cotizacion: Double = this.cotizacion
    ): Moneda {
        val moneda = Moneda()
        moneda.localId = localId // <-- AÑADIDO
        moneda.id = id
        moneda.simbolo = simbolo
        moneda.nombre = nombre
        moneda.cotizacion = cotizacion
        return moneda
    }
}