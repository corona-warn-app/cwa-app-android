package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import de.rki.coronawarnapp.util.serialization.adapter.JsonNodeAdapter
import de.rki.coronawarnapp.util.serialization.adapter.LocalDateAdapter
import okio.ByteString
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate

@Module
class SerializationModule {

    @BaseGson
    @Reusable
    @Provides
    fun baseGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(Duration::class.java, DurationAdapter())
        .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
        .registerTypeAdapter(ByteString::class.java, ByteStringBase64Adapter())
        .registerTypeAdapter(RSAKey.Public::class.java, RSAKey.Public.GsonAdapter())
        .registerTypeAdapter(RSAKey.Private::class.java, RSAKey.Private.GsonAdapter())
        .registerTypeAdapter(JsonNode::class.java, JsonNodeAdapter(jacksonObjectMapper()))
        .create()

    @Reusable
    @Provides
    @BaseJackson
    fun jacksonObjectMapper() = jacksonBaseMapper

    companion object {
        // For access in parcelers, e.g. [DccValidationRule.LogicParceler]
        val jacksonBaseMapper: ObjectMapper by lazy {
            ObjectMapper()
        }
    }
}
