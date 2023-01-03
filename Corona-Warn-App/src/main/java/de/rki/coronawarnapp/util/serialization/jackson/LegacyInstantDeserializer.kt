package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.time.Instant

class LegacyInstantDeserializer : JsonDeserializer<Instant?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Instant? {

        val node = ObjectMapper().readValue(parser, JsonNode::class.java)

        return if (node == null) {
            null
        } else {
            Instant.ofEpochMilli(node["iMillis"].longValue())
        }
    }
}
