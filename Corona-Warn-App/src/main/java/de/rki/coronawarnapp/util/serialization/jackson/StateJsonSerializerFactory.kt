package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import java.io.IOException

class StateJsonSerializerFactory : BeanSerializerModifier() {
    override fun modifySerializer(
        config: SerializationConfig?,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>
    ): JsonSerializer<*> {
        return if (beanDesc.beanClass == State::class.java) {
            StateJsonSerializer(serializer)
        } else serializer
    }

    private class StateJsonSerializer(serializer: JsonSerializer<*>) :
        StdSerializer<State>(serializer.handledType() as Class<State?>) {
        @Throws(IOException::class)
        override fun serialize(state: State, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeObjectField("Valid", State.Valid::class.java)
            jsonGenerator.writeObjectField("ExpiringSoon", State.ExpiringSoon::class.java)
            jsonGenerator.writeObjectField("Expired", State.Expired::class.java)
            jsonGenerator.writeObjectField("Invalid", State.Invalid::class.java)
            jsonGenerator.writeObjectField("Blocked", State.Blocked::class.java)
            jsonGenerator.writeObjectField("Revoked", State.Revoked::class.java)
            jsonGenerator.writeEndObject()
        }
    }
}
