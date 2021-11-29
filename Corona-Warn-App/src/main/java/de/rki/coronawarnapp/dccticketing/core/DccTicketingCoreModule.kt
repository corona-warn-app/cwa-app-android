package de.rki.coronawarnapp.dccticketing.core

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingApiV1
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@Module
class DccTicketingCoreModule {

    @DccTicketingHttpClient
    @Provides
    fun provideHttpClient(@HttpClientDefault client: OkHttpClient): OkHttpClient = client.newBuilder().apply {
        // Remove old logger
        interceptors()
            .removeAll { it is HttpLoggingInterceptor }
            .also { Timber.tag(TAG).d("Removed old HttpLoggingInterceptor %s", it) }

        HttpLoggingInterceptor { message -> Timber.tag(TAG).v(message) }
            .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            .also { addInterceptor(it) }
    }.build()

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

private const val TAG = "DccTicketingOkHttpClient"
