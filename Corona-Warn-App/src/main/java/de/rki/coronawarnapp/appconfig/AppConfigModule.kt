package de.rki.coronawarnapp.appconfig

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.appconfig.mapping.CWAConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.ExposureDetectionConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.ExposureWindowRiskCalculationConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.KeyDownloadParametersMapper
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import okhttp3.OkHttpClient
import org.joda.time.Duration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppConfigModule {

    @Singleton
    @Provides
    fun provideAppConfigApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): AppConfigApiV2 {

        val configHttpClient = client.newBuilder().apply {
            // We no longer use the retrofit cache, due to the complexity it adds when invalidating the cache.
            // The our manual local storage offers more control and should replace it functionally.
            // See **[de.rki.coronawarnapp.appconfig.sources.local.LocalAppConfigSource]**
            // If we ever want to use it again, the previous cache path was:
            // val cacheDir = File(context.cacheDir, "http_app-config")
            // cache(cache)
            connectTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(AppConfigApiV2::class.java)
    }

    @Provides
    fun cwaMapper(mapper: CWAConfigMapper):
        CWAConfig.Mapper = mapper

    @Provides
    fun downloadMapper(mapper: KeyDownloadParametersMapper): KeyDownloadConfig.Mapper = mapper

    @Provides
    fun exposureMapper(mapper: ExposureDetectionConfigMapper):
        ExposureDetectionConfig.Mapper = mapper

    @Provides
    fun windowRiskMapper(mapper: ExposureWindowRiskCalculationConfigMapper):
        ExposureWindowRiskCalculationConfig.Mapper = mapper

    companion object {
        private val HTTP_TIMEOUT_APPCONFIG = Duration.standardSeconds(10)
    }
}
