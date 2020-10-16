package de.rki.coronawarnapp.submission

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.submission.SubmissionCDNServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.RestrictedConnectionSpecs
import de.rki.coronawarnapp.submission.server.SubmissionApiV1
import de.rki.coronawarnapp.submission.server.SubmissionHttpClient
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
class SubmissionModule {

    @Reusable
    @SubmissionHttpClient
    @Provides
    fun cdnHttpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient,
        @RestrictedConnectionSpecs connectionSpecs: List<ConnectionSpec>
    ): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(connectionSpecs).build()

    @Singleton
    @Provides
    fun provideSubmissionApi(
        context: Context,
        @SubmissionHttpClient client: OkHttpClient,
        @SubmissionCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): SubmissionApiV1 {
        val cache = Cache(File(context.cacheDir, "http_submission"), DEFAULT_CACHE_SIZE)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(SubmissionApiV1::class.java)
    }

    @Singleton
    @Provides
    fun provideKeyConverter(defaultKeyConverter: DefaultKeyConverter): KeyConverter =
        defaultKeyConverter

    companion object {
        private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
    }
}
