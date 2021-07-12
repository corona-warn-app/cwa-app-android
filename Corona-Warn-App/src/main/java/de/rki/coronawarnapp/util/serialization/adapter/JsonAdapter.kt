package de.rki.coronawarnapp.util.serialization.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import org.json.JSONObject
import javax.inject.Inject

@Reusable
class JsonNodeAdapter @Inject constructor(
    @BaseJackson val mapper: ObjectMapper
) : TypeAdapter<JsonNode>() {
    override fun write(out: JsonWriter, value: JsonNode?) {
        if (value == null) out.nullValue()
        else out.value(mapper.writeValueAsString(value))
    }

    override fun read(reader: JsonReader): JsonNode? = when (reader.peek()) {
        JSONObject.NULL -> reader.nextNull().let { null }
        else -> mapper.readTree(reader.nextString())
    }
}
