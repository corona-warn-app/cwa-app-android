package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import timber.log.Timber
import java.io.File

inline fun <reified T> ObjectMapper.writeValue(data: T, file: File) = file.bufferedWriter().use { writer ->
    writeValue(writer, data)
    writer.flush()
}

/**
 * Returns null if the file doesn't exist, otherwise returns the parsed object.
 * Throws an exception if the object can't be parsed.
 * An empty file, that was deserialized to a null value is deleted.
 */
inline fun <reified T : Any> ObjectMapper.readValue(file: File): T? {
    if (!file.exists()) {
        Timber.v("fromJson(): File doesn't exist %s", file)
        return null
    }

    return file.bufferedReader().use {
        val value: T? = readValue(it)
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
