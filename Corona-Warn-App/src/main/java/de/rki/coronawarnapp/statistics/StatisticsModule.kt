package de.rki.coronawarnapp.statistics

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.statistics.source.StatisticsApiV1
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.joda.time.Duration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class StatisticsModule {

    @Singleton
    @Provides
    @Statistics
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "statistics")

    @Singleton
    @Provides
    @Statistics
    fun httpCache(
        @Statistics cacheDir: File
    ): Cache = Cache(File(cacheDir, "cache_http"), DEFAULT_CACHE_SIZE)

    @Singleton
    @Provides
    fun api(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
        @Statistics cache: Cache
    ): StatisticsApiV1 {
        val configHttpClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT.millis, TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT.millis, TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT.millis, TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT.millis, TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(StatisticsApiV1::class.java)
    }

    companion object {
        private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
        private val HTTP_TIMEOUT = Duration.standardSeconds(10)
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Statistics
