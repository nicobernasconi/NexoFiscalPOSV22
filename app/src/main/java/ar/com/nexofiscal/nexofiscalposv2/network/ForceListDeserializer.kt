package ar.com.nexofiscal.nexofiscalposv2.network

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class ForceListDeserializer<T>(private val elementAdapter: TypeAdapter<T>) : TypeAdapter<List<T>>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: List<T>?) {
        // No es necesario para la descarga, pero se implementa por completitud
        out.beginArray()
        value?.forEach { elementAdapter.write(out, it) }
        out.endArray()
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): List<T> {
        val list = mutableListOf<T>()
        // Si el token es un array, lo leemos como tal
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray()
            while (reader.hasNext()) {
                list.add(elementAdapter.read(reader))
            }
            reader.endArray()
        }
        // Si es un objeto, lo leemos y lo envolvemos en una lista
        else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            list.add(elementAdapter.read(reader))
        }
        // Si es nulo, lo saltamos
        else if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
        }
        // Si es cualquier otra cosa, es un error
        else {
            throw JsonParseException("Tipo de elemento inesperado: se esperaba un objeto o un array.")
        }
        return list
    }
}