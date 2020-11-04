package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.adapter.ByteArrayAdapter

@Module
class SerializationModule {

    @BaseGson
    @Reusable
    @Provides
    fun baseGson(): Gson = GsonBuilder()
        .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
        .create()
}
