package de.rki.coronawarnapp.verification

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.RestrictedConnectionSpecs
import de.rki.coronawarnapp.verification.server.VerificationApiV1
import de.rki.coronawarnapp.verification.server.VerificationHttpClient
import de.rki.coronawarnapp.verification.server.VerificationServerUrl
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        context: Context,
        @VerificationHttpClient client: OkHttpClient,
        @VerificationServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): VerificationApiV1 {
        val cache = Cache(File(context.cacheDir, "http_verification"), DEFAULT_CACHE_SIZE)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(VerificationApiV1::class.java)
    }

    @Singleton
    @VerificationServerUrl
    @Provides
    fun provideVerificationUrl(): String {
        val url = BuildConfig.VERIFICATION_CDN_URL
        if (!url.startsWith("https://")) throw IllegalStateException("Innvalid: $url")
        return url
    }

    companion object {
        private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
    }
}
