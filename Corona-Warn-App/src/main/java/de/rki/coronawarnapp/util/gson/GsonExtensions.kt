package de.rki.coronawarnapp.util.gson

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

inline fun <reified T> Gson.fromJson(json: String): T = fromJson(
    json,
    object : TypeToken<T>() {}.type
)

inline fun <reified T> Gson.fromJson(file: File): T = file.reader().use {
    fromJson(it, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Gson.toJson(data: T, file: File) = file.writer().use { writer ->
    toJson(data, writer)
}
