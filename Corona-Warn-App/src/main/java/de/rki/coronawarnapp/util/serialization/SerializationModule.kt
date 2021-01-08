package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.adapter.ByteArrayAdapter
import de.rki.coronawarnapp.util.serialization.adapter.DurationAdapter
import de.rki.coronawarnapp.util.serialization.adapter.InstantAdapter
import de.rki.coronawarnapp.util.serialization.adapter.LocalDateAdapter
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
        .create()
}
