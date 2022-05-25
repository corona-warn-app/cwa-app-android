package de.rki.coronawarnapp.util.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.fasterxml.jackson.databind.ObjectMapper
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

abstract class BaseJsonSerializer<T : Any>(
    private val objectMapper: ObjectMapper
) : Serializer<T> {

    private val type get() = defaultValue::class.java

    override suspend fun readFrom(input: InputStream): T = runCatching { objectMapper.readValue(input, type) }
        .onFailure { throw CorruptionException("Failed to read data of type=$type", it) }
        .getOrThrow()

    override suspend fun writeTo(t: T, output: OutputStream) {
        runCatching { objectMapper.writeValue(output, t) }
            .onFailure { Timber.tag(TAG).w(it, "Failed to write data=$t") }
    }
}

private const val TAG = "BaseJsonSerializer"
