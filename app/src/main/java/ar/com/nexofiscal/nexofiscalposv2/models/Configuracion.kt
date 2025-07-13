package ar.com.nexofiscal.nexofiscalposv2.models

data class CodigoBarraConfig(
    val inicio: Int = 0,
    val id_long: Int = 0,
    val payload_type: String = "",
    val payload_int: Int = 0,
    val long: Int = 0
)

data class BalanzaConfig(
    val puerto: String = "",
    val baudios: Int = 9600,
    val reintentos: Int = 3
)

data class Configuracion(
    val tiempoDescargaMinutos: Int = 60,
    val tiempoSubidaMinutos: Int = 15,
    val codigoBarra: CodigoBarraConfig = CodigoBarraConfig(),
    val balanza: BalanzaConfig = BalanzaConfig()
)