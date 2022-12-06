package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import java.io.IOException
import java.time.Instant

class SelectedStatisticsLocationFactory : BeanSerializerModifier() {

    override fun modifySerializer(
        config: SerializationConfig?,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>
    ): JsonSerializer<*> {
        return if (beanDesc.beanClass == SelectedStatisticsLocation::class.java) {
            SelectedStatisticsLocationSerializer(serializer)
        } else serializer
    }

    class SelectedStatisticsLocationSerializer(val serializer: JsonSerializer<*>) :
        StdSerializer<SelectedStatisticsLocation>(serializer.handledType() as Class<SelectedStatisticsLocation>) {

        @Throws(IOException::class)
        override fun serialize(
            location: SelectedStatisticsLocation,
            jsonGenerator: JsonGenerator,
            serializerProvider: SerializerProvider
        ) {
            when (location) {
                is SelectedStatisticsLocation.SelectedDistrict -> {
                    jsonGenerator.writeStartObject()
                    jsonGenerator.writeStringField("type", "district")
                    jsonGenerator.writeObjectField("district", Districts.District::class)
                    jsonGenerator.writeObjectField("addedAt", Instant::class)
                    jsonGenerator.writeEndObject()
                }
                is SelectedStatisticsLocation.SelectedFederalState -> {
                    jsonGenerator.writeStartObject()
                    jsonGenerator.writeStringField("type", "federalState")
                    jsonGenerator.writeObjectField("federalState", PpaData.PPAFederalState::class)
                    jsonGenerator.writeObjectField("addedAt", Instant::class)
                    jsonGenerator.writeEndObject()
                }
            }
        }
    }
}
