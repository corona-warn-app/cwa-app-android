package de.rki.coronawarnapp.covidcertificate.validation.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.covidcertificate.validation.core.settings.DccValidationSettings
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSet
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [DccValidationModule.ResetModule::class])
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

    @Singleton
    @CertificateValidationDataStore
    @Provides
    fun provideDccValidationSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope,
        produceFile = { context.preferencesDataStoreFile("dcc_validation_datastore") }
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDccValidationServer(resettable: DccValidationServer): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccValidationCache(resettable: DccValidationCache): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccValidationSettings(resettable: DccValidationSettings): Resettable
    }
}

private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CertificateValidation

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CertificateValidationDataStore
