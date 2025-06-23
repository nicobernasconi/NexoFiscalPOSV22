package ar.com.nexofiscal.nexofiscalposv2.models

class Familia {
    var id: Int = 0
    var numero: Int? = null
    var nombre: String? = null

    constructor()

    constructor(id: Int, numero: Int?, nombre: String?) {
        this.id = id
        this.numero = numero
        this.nombre = nombre
    }

    override fun toString(): String {
        return "Familia{" +
                "id=" + id +
                ", numero=" + numero +
                ", nombre='" + nombre + '\'' +
                '}'
    }
    fun copy(id: Int = this.id, numero: Int? = this.numero, nombre: String? = this.nombre): Familia {
        return Familia(id, numero, nombre)
    }
}
