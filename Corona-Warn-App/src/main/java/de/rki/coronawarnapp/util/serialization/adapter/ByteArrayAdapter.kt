package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.json.JSONObject.NULL

class ByteArrayAdapter : TypeAdapter<ByteArray>() {
    override fun write(out: JsonWriter, value: ByteArray?) {
        if (value == null) out.nullValue()
        else value.toByteString().base64().let { out.value(it) }
    }

    override fun read(reader: JsonReader): ByteArray? = when (reader.peek()) {
        NULL -> reader.nextNull().let { null }
        else -> {
            val raw = reader.nextString()
            raw.decodeBase64()?.toByteArray() ?: throw JsonParseException("Can't decode base64 ByteArray: $raw")
        }
    }
}
