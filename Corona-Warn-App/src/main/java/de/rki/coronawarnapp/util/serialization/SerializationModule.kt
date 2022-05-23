package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import de.rki.coronawarnapp.util.serialization.adapter.ByteArrayAdapter
import de.rki.coronawarnapp.util.serialization.adapter.ByteStringBase64Adapter
import de.rki.coronawarnapp.util.serialization.adapter.DurationAdapter
import de.rki.coronawarnapp.util.serialization.adapter.InstantAdapter
import de.rki.coronawarnapp.util.serialization.adapter.JavaInstantAdapter
import de.rki.coronawarnapp.util.serialization.adapter.JsonNodeAdapter
import de.rki.coronawarnapp.util.serialization.adapter.LocalDateAdapter
import de.rki.coronawarnapp.util.serialization.jackson.registerByteStringSerialization
import okio.ByteString
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate

@Module
class SerializationModule {

    @BaseGson
    @Reusable
    @Provides
    fun baseGson(): Gson = baseGson

    @Reusable
    @Provides
    @BaseJackson
    fun jacksonObjectMapper() = jacksonBaseMapper

    companion object {
        val jacksonBaseMapper: ObjectMapper by lazy {
            val jacksonSerializationModule = SimpleModule()
                .registerByteStringSerialization()

            jsonMapper {
                addModules(kotlinModule(), JodaModule(), jacksonSerializationModule)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }

        val baseGson: Gson by lazy {
            GsonBuilder()
                .registerTypeAdapter(Instant::class.java, InstantAdapter())
                .registerTypeAdapter(java.time.Instant::class.java, JavaInstantAdapter())
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                .registerTypeAdapter(Duration::class.java, DurationAdapter())
                .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
                .registerTypeAdapter(ByteString::class.java, ByteStringBase64Adapter())
                .registerTypeAdapter(RSAKey.Public::class.java, RSAKey.Public.GsonAdapter())
                .registerTypeAdapter(RSAKey.Private::class.java, RSAKey.Private.GsonAdapter())
                .registerTypeAdapter(JsonNode::class.java, JsonNodeAdapter(jacksonObjectMapper()))
                .create()
        }
    }
}
