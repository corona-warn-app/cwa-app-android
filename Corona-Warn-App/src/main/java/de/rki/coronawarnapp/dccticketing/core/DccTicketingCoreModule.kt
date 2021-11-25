package de.rki.coronawarnapp.dccticketing.core

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingApiV1
import de.rki.coronawarnapp.http.build
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Module
class DccTicketingCoreModule {

    @DccTicketingHttpClient
    @Provides
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder().build(enableLogging = true)

    @Reusable
    @Provides
    fun provideDccTicketingValidationApiV1(
        @DccTicketingHttpClient client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccTicketingApiV1 = Retrofit.Builder()
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .build()
        .create(DccTicketingApiV1::class.java)
}

// Dummy base url to satisfy Retrofit ¯\_(ツ)_/¯
private const val BASE_URL = "https://localhost.de"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccTicketing
