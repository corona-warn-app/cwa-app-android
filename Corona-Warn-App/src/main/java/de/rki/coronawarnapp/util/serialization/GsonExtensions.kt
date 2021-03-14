package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import kotlin.reflect.KClass

inline fun <reified T> Gson.fromJson(json: String): T = fromJson(
    json,
    object : TypeToken<T>() {}.type
)

/**
 * Returns null if the file doesn't exist, otherwise returns the parsed object.
 * Throws an exception if the object can't be parsed.
 * An empty file, that was deserialized to a null value is deleted.
 */
inline fun <reified T : Any> Gson.fromJson(file: File): T? {
    if (!file.exists()) {
        Timber.v("fromJson(): File doesn't exist %s", file)
        return null
    }

    return file.bufferedReader().use {
        val value: T? = fromJson(it, object : TypeToken<T>() {}.type)
        if (value != null) {
            Timber.v("Json read from %s", file)
            value
        } else {
            Timber.w("Tried to parse json from file that exists, but was empty: %s", file)
            if (file.delete()) Timber.w("Deleted empty json file: %s", file)
            null
        }
    }
}

inline fun <reified T> Gson.toJson(data: T, file: File) = file.bufferedWriter().use { writer ->
    toJson(data, writer)
    writer.flush()
}

fun <T : Any> KClass<T>.getDefaultGsonTypeAdapter(): TypeAdapter<T> = Gson().getAdapter(this.java)
