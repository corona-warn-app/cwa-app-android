package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }

    override fun read(reader: JsonReader): LocalDate? = when (reader.peek()) {
        JsonToken.NULL -> reader.nextNull().let { null }
        else -> LocalDate.parse(reader.nextString())
    }
}
