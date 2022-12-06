package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.rki.coronawarnapp.submission.Symptoms
import java.io.IOException

class SymptomsJsonSerializerFactory : BeanSerializerModifier() {
    override fun modifySerializer(
        config: SerializationConfig?,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>
    ): JsonSerializer<*> {
        return if (beanDesc.beanClass == Symptoms.StartOf::class.java) {
            StateJsonSerializer(serializer)
        } else serializer
    }

    private class StateJsonSerializer(serializer: JsonSerializer<*>) :
        StdSerializer<Symptoms.StartOf>(serializer.handledType() as Class<Symptoms.StartOf?>) {
        @Throws(IOException::class)
        override fun serialize(
            state: Symptoms.StartOf,
            jsonGenerator: JsonGenerator,
            serializerProvider: SerializerProvider
        ) {
            jsonGenerator.writeStartObject()
            jsonGenerator.writeObjectField("LastSevenDays", Symptoms.StartOf.LastSevenDays::class.java)
            jsonGenerator.writeObjectField("MoreThanTwoWeeks", Symptoms.StartOf.MoreThanTwoWeeks::class.java)
            jsonGenerator.writeObjectField("NoInformation", Symptoms.StartOf.NoInformation::class.java)
            jsonGenerator.writeObjectField("OneToTwoWeeksAgo", Symptoms.StartOf.OneToTwoWeeksAgo::class.java)
            jsonGenerator.writeObjectField("Date", Symptoms.StartOf.Date::class.java)
            jsonGenerator.writeEndObject()
        }
    }
}
