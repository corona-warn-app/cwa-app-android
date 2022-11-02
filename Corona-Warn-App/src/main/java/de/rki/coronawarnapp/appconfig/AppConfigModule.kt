package de.rki.coronawarnapp.appconfig

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
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
import de.rki.coronawarnapp.appconfig.mapping.SelfReportSubmissionConfigMapper
import de.rki.coronawarnapp.appconfig.mapping.SurveyConfigMapper
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.time.Duration
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [AppConfigModule.BindsModule::class, AppConfigModule.ResetModule::class])
object AppConfigModule {

    @Singleton
    @Provides
    fun provideAppConfigApi(
        @RemoteAppConfigCache cache: Cache,
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
    ): AppConfigApiV2 {

        val configHttpClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT_APPCONFIG.toMillis(), TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT_APPCONFIG.toMillis(), TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT_APPCONFIG.toMillis(), TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT_APPCONFIG.toMillis(), TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl(url)
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

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableAppConfigProvider(resettable: AppConfigProvider): Resettable
    }

    @Module
    internal interface BindsModule {

        @Binds
        fun cwaMapper(mapper: CWAConfigMapper): CWAConfig.Mapper

        @Binds
        fun downloadMapper(mapper: KeyDownloadParametersMapper): KeyDownloadConfig.Mapper

        @Binds
        fun exposureMapper(mapper: ExposureDetectionConfigMapper): ExposureDetectionConfig.Mapper

        @Binds
        fun windowRiskMapper(mapper: ExposureWindowRiskCalculationConfigMapper):
            ExposureWindowRiskCalculationConfig.Mapper

        @Binds
        fun surveyMapper(mapper: SurveyConfigMapper): SurveyConfig.Mapper

        @Binds
        fun analyticsMapper(mapper: AnalyticsConfigMapper): AnalyticsConfig.Mapper

        @Binds
        fun logUploadMapper(mapper: LogUploadConfigMapper): LogUploadConfig.Mapper

        @Binds
        fun presenceTracingMapper(mapper: PresenceTracingConfigMapper): PresenceTracingConfig.Mapper

        @Binds
        fun coronaTestConfigMapper(mapper: CoronaTestConfigMapper): CoronaTestConfig.Mapper

        @Binds
        fun covidCertificateConfigMapper(mapper: CovidCertificateConfigMapper): CovidCertificateConfig.Mapper

        @Binds
        fun selfReportSubmissionConfigMapper(
            mapper: SelfReportSubmissionConfigMapper
        ): SelfReportSubmissionConfig.Mapper
    }
}

private val HTTP_TIMEOUT_APPCONFIG = Duration.ofSeconds(10)
private const val DEFAULT_CACHE_SIZE = 2 * 1024 * 1024L // 5MB
