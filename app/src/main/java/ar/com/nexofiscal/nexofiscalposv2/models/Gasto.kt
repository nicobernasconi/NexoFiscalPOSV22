package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Gasto retornado por la API.
 */
data class Gasto(
    @SerializedName("id") val id: Int,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("monto") val monto: Double?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("usuario_id") val usuarioId: Int?,
    @SerializedName("empresa_id") val empresaId: Int?,
    @SerializedName("tipo_gasto_id") val tipoGastoId: Int?,
    @SerializedName("tipo_gasto") val tipoGasto: String?,
    @SerializedName("cierre_caja_id") val cierreCajaId: Int?
)
