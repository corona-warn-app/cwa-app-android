package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.json.JSONObject.NULL

class ByteArrayAdapter : TypeAdapter<ByteArray>() {
    override fun write(out: JsonWriter, value: ByteArray?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(String(value))
        }
    }

    override fun read(reader: JsonReader): ByteArray? = when (reader.peek()) {
        NULL -> {
            reader.nextNull()
            null
        }
        else -> {
            reader.nextString().toByteArray()
        }
    }
}
