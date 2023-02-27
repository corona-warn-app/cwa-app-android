package de.rki.coronawarnapp.coronatest.server

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.verification.VerificationCDNServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.RestrictedConnectionSpecs
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
class VerificationModule {

    @Reusable
    @VerificationHttpClient
    @Provides
    fun cdnHttpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient,
        @RestrictedConnectionSpecs connectionSpecs: List<ConnectionSpec>
    ): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(connectionSpecs).build()

    @Singleton
    @Provides
    fun provideVerificationApi(
        @AppContext context: Context,
        @VerificationHttpClient client: OkHttpClient,
        @VerificationCDNServerUrl url: String,
        jacksonConverterFactory: JacksonConverterFactory
    ): VerificationApiV1 {
        val cache = Cache(File(context.cacheDir, "http_verification"), DEFAULT_CACHE_SIZE)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(jacksonConverterFactory)
            .build()
            .create(VerificationApiV1::class.java)
    }

    companion object {
        private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
    }
}
