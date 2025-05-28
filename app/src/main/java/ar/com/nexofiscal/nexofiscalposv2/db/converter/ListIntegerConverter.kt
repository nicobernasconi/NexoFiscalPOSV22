package ar.com.nexofiscal.nexofiscalposv2.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListIntegerConverter {
    @TypeConverter
    fun fromString(value: String?): List<Int>? {
        val listType = object : TypeToken<List<Int>>() {}.type
        return if (value == null) null else Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return if (list == null) null else Gson().toJson(list)
    }
}