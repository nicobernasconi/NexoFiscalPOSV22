package ar.com.nexofiscal.nexofiscalposv2.models

class Familia {
    var id: Int = 0
    var localId: Int = 0
    var numero: Int? = null
    var nombre: String? = null

    constructor()



    override fun toString(): String {
        return "Familia{" +
                "id=" + id +
                ", numero=" + numero +
                ", nombre='" + nombre + '\'' +
                '}'
    }
    fun copy(
        localId: Int = this.localId,
        id: Int = this.id,
        numero: Int? = this.numero,
        nombre: String? = this.nombre
    ): Familia {
        val familia = Familia()
        familia.localId = localId
        familia.id = id
        familia.numero = numero
        familia.nombre = nombre
        return familia
    }
}
