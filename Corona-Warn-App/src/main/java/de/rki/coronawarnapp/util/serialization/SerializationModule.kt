package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.util.serialization.jackson.registerByteArraySerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerByteStringSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerDurationSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerInstantSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerLocalDateSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerPPADataSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerPrivateSerialization
import de.rki.coronawarnapp.util.serialization.jackson.registerPublicSerialization

@InstallIn(SingletonComponent::class)
@Module
class SerializationModule {

    @Reusable
    @Provides
    @BaseJackson
    fun jacksonObjectMapper() = jacksonBaseMapper

    companion object {
        val jacksonBaseMapper: ObjectMapper by lazy {
            val jacksonSerializationModule = SimpleModule()
                .registerByteStringSerialization()
                .registerInstantSerialization()
                .registerLocalDateSerialization()
                .registerPublicSerialization()
                .registerPrivateSerialization()
                .registerDurationSerialization()
                .registerPPADataSerialization()
                .registerByteArraySerialization()

            jsonMapper {
                addModules(kotlinModule(), JavaTimeModule(), jacksonSerializationModule)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}
