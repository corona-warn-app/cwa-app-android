package de.rki.coronawarnapp.vaccination.core

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.json.JSONObject

data class RawCOSEObject(
    private val data: ByteString
) {
    constructor(data: ByteArray) : this(data.toByteString())

    val asByteArray: ByteArray
        get() = data.toByteArray()

    companion object {
        val EMPTY = RawCOSEObject(data = ByteString.EMPTY)
    }

    class JsonAdapter : TypeAdapter<RawCOSEObject>() {
        override fun write(out: JsonWriter, value: RawCOSEObject?) {
            if (value == null) out.nullValue()
            else value.data.base64().let { out.value(it) }
        }

        override fun read(reader: JsonReader): RawCOSEObject? = when (reader.peek()) {
            JSONObject.NULL -> reader.nextNull().let { null }
            else -> {
                val raw = reader.nextString()
                raw.decodeBase64()?.let { RawCOSEObject(data = it) }
                    ?: throw JsonParseException("Can't decode base64 ByteArray: $raw")
            }
        }
    }
}


