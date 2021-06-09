package de.rki.coronawarnapp.appconfig

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.appconfig.mapping.AnalyticsConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.CWAConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.CoronaTestConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.CovidCertificateConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.ExposureDetectionConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.ExposureWindowRiskCalculationConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.KeyDownloadParametersMapper
import de.rki.coronawarnapp.appconfig.mapping.LogUploadConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.PresenceTracingConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.SurveyConfigMapper
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
    fun provideAppConfigApi(
        @RemoteAppConfigCache cache: Cache,
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): AppConfigApiV2 {

        val configHttpClient = client.newBuilder().apply {
            cache(cache)
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

    @RemoteAppConfigCache
    @Provides
    @Singleton
    fun remoteAppConfigHttpCache(@AppContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_app-config")
        return Cache(cacheDir, DEFAULT_CACHE_SIZE)
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

    @Provides
    fun surveyMapper(mapper: SurveyConfigMapper):
        SurveyConfig.Mapper = mapper

    @Provides
    fun analyticsMapper(mapper: AnalyticsConfigMapper):
        AnalyticsConfig.Mapper = mapper

    @Provides
    fun logUploadMapper(mapper: LogUploadConfigMapper):
        LogUploadConfig.Mapper = mapper

    @Provides
    fun presenceTracingMapper(mapper: PresenceTracingConfigMapper):
        PresenceTracingConfig.Mapper = mapper

    @Provides
    fun coronaTestConfigMapper(mapper: CoronaTestConfigMapper):
        CoronaTestConfig.Mapper = mapper

    @Provides
    fun covidCertificateConfigMapper(mapper: CovidCertificateConfigMapper):
        CovidCertificateConfig.Mapper = mapper

    companion object {
        private val HTTP_TIMEOUT_APPCONFIG = Duration.standardSeconds(10)
        private const val DEFAULT_CACHE_SIZE = 2 * 1024 * 1024L // 5MB
    }
}
