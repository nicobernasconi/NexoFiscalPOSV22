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

    fun copy(id: Int = this.id, fecha: String? = this.fecha, totalVentas: Double? = this.totalVentas, totalGastos: Double? = this.totalGastos, efectivoInicial: Double? = this.efectivoInicial, efectivoFinal: Double? = this.efectivoFinal, tipoCajaId: Int? = this.tipoCajaId, usuario: Usuario? = this.usuario): CierreCaja {
        val cierreCaja = CierreCaja()
        cierreCaja.id = id
        cierreCaja.fecha = fecha
        cierreCaja.totalVentas = totalVentas
        cierreCaja.totalGastos = totalGastos
        cierreCaja.efectivoInicial = efectivoInicial
        cierreCaja.efectivoFinal = efectivoFinal
        cierreCaja.tipoCajaId = tipoCajaId
        cierreCaja.usuario = usuario?.copy() // Asumiendo que Usuario tiene un método copy()
        return cierreCaja
    }


}
