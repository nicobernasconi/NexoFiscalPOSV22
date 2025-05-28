// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/CierreCaja.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class CierreCaja {
    // Getters & Setters
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("fecha")
    var fecha: String? = null

    @SerializedName("total_ventas")
    var totalVentas: Double? = null

    @SerializedName("total_gastos")
    var totalGastos: Double? = null

    @SerializedName("efectivo_inicial")
    var efectivoInicial: Double? = null

    @SerializedName("efectivo_final")
    var efectivoFinal: Double? = null

    @SerializedName("tipo_caja_id")
    var tipoCajaId: Int? = null // puede ser null

    @SerializedName("usuario")
    var usuario: Usuario? = null

    override fun toString(): String {
        return "CierreCaja{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                ", totalVentas=" + totalVentas +
                ", totalGastos=" + totalGastos +
                ", efectivoInicial=" + efectivoInicial +
                ", efectivoFinal=" + efectivoFinal +
                ", tipoCajaId=" + tipoCajaId +
                ", usuario=" + usuario +
                '}'
    }
}
