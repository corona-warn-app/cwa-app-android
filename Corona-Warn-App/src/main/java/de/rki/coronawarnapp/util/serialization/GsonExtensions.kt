package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.reflect.KClass

inline fun <reified T> Gson.fromJson(json: String): T = fromJson(
    json,
    object : TypeToken<T>() {}.type
)

inline fun <reified T> Gson.fromJson(file: File): T = file.bufferedReader().use {
    fromJson(it, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Gson.toJson(data: T, file: File) = file.bufferedWriter().use { writer ->
    toJson(data, writer)
    writer.flush()
}

fun <T : Any> KClass<T>.getDefaultGsonTypeAdapter(): TypeAdapter<T> = Gson().getAdapter(this.java)
