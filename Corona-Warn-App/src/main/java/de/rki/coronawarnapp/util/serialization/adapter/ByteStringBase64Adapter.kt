package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import org.json.JSONObject.NULL

class ByteStringBase64Adapter : TypeAdapter<ByteString>() {
    override fun write(out: JsonWriter, value: ByteString?) {
        if (value == null) out.nullValue()
        else value.base64().let { out.value(it) }
    }

    override fun read(reader: JsonReader): ByteString? = when (reader.peek()) {
        NULL -> reader.nextNull().let { null }
        else -> {
            val raw = reader.nextString()
            raw.decodeBase64() ?: throw JsonParseException("Can't decode base64 ByteString: $raw")
        }
    }
}
