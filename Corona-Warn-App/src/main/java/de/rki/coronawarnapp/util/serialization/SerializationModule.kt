package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class SerializationModule {

    @BaseGson
    @Reusable
    @Provides
    fun baseGson(): Gson = GsonBuilder().create()
}
