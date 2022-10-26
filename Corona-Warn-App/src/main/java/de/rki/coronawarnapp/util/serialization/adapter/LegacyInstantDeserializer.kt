package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.Instant

class LegacyInstantDeserializer : JsonDeserializer<Instant> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Instant? {
        val jsonObject = json?.asJsonObject ?: return null
        return Instant.ofEpochMilli(jsonObject.get("iMillis").asLong)
    }
}
