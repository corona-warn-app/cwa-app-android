package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import timber.log.Timber

class CoronaTestResultSerializer : JsonSerializer<CoronaTestResult>() {
    override fun serialize(value: CoronaTestResult?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen?.writeNumber(value.value)
        }
    }
}

class CoronaTestResultDeSerializer : JsonDeserializer<CoronaTestResult>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CoronaTestResult {
        requireNotNull(p) { "JsonParser is null" }
        val node = ObjectMapper().readValue(p, JsonNode::class.java)
        return if (node.isTextual) {
            CoronaTestResult.valueOf(node.asText())
        } else {
            CoronaTestResult.fromInt(p.intValue)
        }
    }
}

fun SimpleModule.registerCoronaTestResultSerialization() = apply {
    addSerializer(CoronaTestResult::class, CoronaTestResultSerializer())
    addDeserializer(CoronaTestResult::class, CoronaTestResultDeSerializer())
}
