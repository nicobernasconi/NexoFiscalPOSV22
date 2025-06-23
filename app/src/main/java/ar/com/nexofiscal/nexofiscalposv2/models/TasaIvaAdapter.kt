package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class TasaIvaAdapter : TypeAdapter<TasaIva>() {

    private val gson = Gson()

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: TasaIva?) {
        // La escritura no es relevante para la descarga, se delega a GSON.
        gson.toJson(value, TasaIva::class.java, out)
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): TasaIva? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> {
                // Si la API envía un número, lo interpretamos como el ID.
                val id = reader.nextInt()
                TasaIva().apply { this.id = id }
            }
            JsonToken.BEGIN_OBJECT -> {
                // Si la API envía un objeto completo, lo parseamos normalmente.
                gson.fromJson(reader, TasaIva::class.java)
            }
            else -> {
                // Si es otro tipo de dato, lo ignoramos.
                reader.skipValue()
                null
            }
        }
    }
}