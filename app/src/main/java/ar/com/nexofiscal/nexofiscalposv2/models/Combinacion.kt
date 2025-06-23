package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la entidad Combinación de productos.
 */
class Combinacion {
    // --- Getters & Setters ---
    @SerializedName("producto_principal_id")
    var productoPrincipalId: Int = 0

    @SerializedName("subproducto_id")
    var subproductoId: Int = 0

    @SerializedName("cantidad")
    var cantidad: Double = 0.0

    @SerializedName("empresa_id")
    var empresaId: Int = 0

    constructor()

    constructor(productoPrincipalId: Int, subproductoId: Int, cantidad: Double, empresaId: Int) {
        this.productoPrincipalId = productoPrincipalId
        this.subproductoId = subproductoId
        this.cantidad = cantidad
        this.empresaId = empresaId
    }


    override fun toString(): String {
        return "Combinacion{" +
                ", productoPrincipalId=" + productoPrincipalId +
                ", subproductoId=" + subproductoId +
                ", cantidad=" + cantidad +
                ", empresaId=" + empresaId +
                '}'
    }
    fun copy(productoPrincipalId: Int = this.productoPrincipalId, subproductoId: Int = this.subproductoId, cantidad: Double = this.cantidad, empresaId: Int = this.empresaId): Combinacion {
        return Combinacion(productoPrincipalId, subproductoId, cantidad, empresaId)
    }
}

