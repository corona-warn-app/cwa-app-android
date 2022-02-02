package de.rki.coronawarnapp.util.serialization.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

@Reusable
class JsonNodeAdapter @Inject constructor(
    @BaseJackson val mapper: ObjectMapper,
) : TypeAdapter<JsonNode>() {

    private val gson by lazy {
        // We don't inject `baseGson` as we don't want any specific type mapping to happen.
        // Would also cause a cyclic dependency: BaseGson needing this adapter and this adapter needing BaseGson
        Gson()
    }

    override fun write(out: JsonWriter, value: JsonNode?) {
        if (value == null) out.nullValue()
        else {
            val text = value.toString()
            out.jsonValue(text)
        }
    }

    override fun read(reader: JsonReader): JsonNode? = when (reader.peek()) {
        JsonToken.NULL -> reader.nextNull().let { null }
        else -> {
            // We take a JSON object, parse it a Gson object and parse that as a Jackson object ᕦ(ò_óˇ)
            val gsonJson: JsonElement = gson.fromJson(reader, JsonElement::class.java)
            mapper.readTree(gsonJson.toString())
        }
    }
}
