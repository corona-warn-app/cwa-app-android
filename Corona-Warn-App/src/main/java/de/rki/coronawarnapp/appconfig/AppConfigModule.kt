package de.rki.coronawarnapp.appconfig

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.mapping.CWAConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.ExposureDetectionConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.KeyDownloadParametersMapper
import de.rki.coronawarnapp.appconfig.mapping.RiskCalculationConfigMapper
import de.rki.coronawarnapp.appconfig.sources.remote.AppConfigApiV1
import de.rki.coronawarnapp.appconfig.sources.remote.AppConfigHttpCache
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.joda.time.Duration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppConfigModule {

    @Singleton
    @Provides
    @AppConfigHttpCache
    fun provideAppConfigCache(
        @AppContext context: Context
    ): Cache {
        val cacheSize = 1 * 1024 * 1024L // 1MB
        val cacheDir = File(context.cacheDir, "http_app-config")
        return Cache(cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    fun provideAppConfigApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
        @AppConfigHttpCache cache: Cache
    ): AppConfigApiV1 {

        val cachingClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(AppConfigApiV1::class.java)
    }

    @Provides
    fun cwaMapper(mapper: CWAConfigMapper): CWAConfig.Mapper = mapper

    @Provides
    fun downloadMapper(mapper: KeyDownloadParametersMapper): KeyDownloadConfig.Mapper = mapper

    @Provides
    fun exposurMapper(mapper: ExposureDetectionConfigMapper): ExposureDetectionConfig.Mapper =
        mapper

    @Provides
    fun riskMapper(mapper: RiskCalculationConfigMapper): RiskCalculationConfig.Mapper = mapper

    companion object {
        private val HTTP_TIMEOUT_APPCONFIG = Duration.standardSeconds(10)
    }
}
