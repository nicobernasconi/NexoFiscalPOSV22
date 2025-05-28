// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Sucursal.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Sucursal {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("empresa_id")
    var empresaId: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("direccion")
    var direccion: String? = null

    @SerializedName("telefono")
    var telefono: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("contacto_nombre")
    var contactoNombre: String? = null

    @SerializedName("contacto_telefono")
    var contactoTelefono: String? = null

    @SerializedName("contacto_email")
    var contactoEmail: String? = null

    @SerializedName("referente_nombre")
    var referenteNombre: String? = null

    @SerializedName("referente_telefono")
    var referenteTelefono: String? = null

    @SerializedName("referente_email")
    var referenteEmail: String? = null

    override fun toString(): String {
        return "Sucursal{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}
