package de.rki.coronawarnapp.dccticketing.core

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingApiV1
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.util.serialization.BaseGson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Module
class DccTicketingCoreModule {

    @DccTicketing
    @Provides
    fun provideHttpClient(@HttpClientDefault client: OkHttpClient) = client

    @DccTicketing
    @Provides
    fun provideGson(@BaseGson gson: Gson) = gson

    @Reusable
    @Provides
    fun provideDccTicketingValidationApiV1(
        @DccTicketing client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccTicketingApiV1 = Retrofit.Builder()
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .build()
        .create(DccTicketingApiV1::class.java)
}

// Dummy base url to satisfy Retrofit ¯\_(ツ)_/¯
private const val BASE_URL = "http://localhost.de"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccTicketing
