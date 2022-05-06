package de.rki.coronawarnapp.covidcertificate.validation.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSet
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [DccValidationModule.BindsModule::class])
object DccValidationModule {

    @Singleton
    @Provides
    @CertificateValidation
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "dcc_validation")

    @Singleton
    @Provides
    @CertificateValidation
    fun httpCache(
        @Statistics cacheDir: File
    ): Cache = Cache(File(cacheDir, "dcc_country_cache_http"), DEFAULT_CACHE_SIZE)

    @Reusable
    @Provides
    fun countryApi(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CertificateValueSet cache: Cache
    ): DccCountryApi {
        val client = httpClient.newBuilder()
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .build()
            .create(DccCountryApi::class.java)
    }

    @Reusable
    @Provides
    fun rulesApi(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CertificateValueSet cache: Cache
    ): DccValidationRuleApi = Retrofit.Builder()
        .client(
            httpClient.newBuilder()
                .cache(cache)
                .build()
        )
        .baseUrl(url)
        .build()
        .create(DccValidationRuleApi::class.java)

    @Module
    internal interface BindsModule {

        @Binds
        @IntoSet
        fun bindResettableDccValidationServer(resettable: DccValidationServer): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccValidationCache(resettable: DccValidationCache): Resettable
    }
}

private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CertificateValidation
