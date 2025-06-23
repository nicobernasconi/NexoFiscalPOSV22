// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Vendedor.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Vendedor {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("direccion")
    var direccion: String? = null // puede ser null

    @SerializedName("telefono")
    var telefono: String? = null // puede ser null

    @SerializedName("porcentaje_comision")
    var porcentajeComision: Double? = null // puede ser null

    @SerializedName("fecha_ingreso")
    var fechaIngreso: String? = null // formato "YYYY-MM-DD"

    override fun toString(): String {
        return "Vendedor{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre, direccion: String? = this.direccion, telefono: String? = this.telefono, porcentajeComision: Double? = this.porcentajeComision, fechaIngreso: String? = this.fechaIngreso): Vendedor {
        val vendedor = Vendedor()
        vendedor.id = id
        vendedor.nombre = nombre
        vendedor.direccion = direccion
        vendedor.telefono = telefono
        vendedor.porcentajeComision = porcentajeComision
        vendedor.fechaIngreso = fechaIngreso
        return vendedor
    }
}
