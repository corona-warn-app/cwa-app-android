package de.rki.coronawarnapp.bugreporting.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.Instant

class Converters {

    private val gson = Gson()
    private val typeStringList = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun toInstant(from: String?): Instant? = from?.let { Instant.parse(it) }

    @TypeConverter
    fun fromInstant(instant: Instant?): String? = instant?.toString()

    @TypeConverter
    fun toStringList(string: String?): List<String>? =
        string?.let { gson.fromJson(it, typeStringList) }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String? =
        strings?.let { gson.toJson(it, typeStringList) }
}
