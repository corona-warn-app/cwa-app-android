package de.rki.coronawarnapp.covidcertificate.valueset

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetApiV1
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetServer
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [CertificateValueSetModule.ResetModule::class])
object CertificateValueSetModule {

    @Reusable
    @CertificateValueSet
    @Provides
    fun cache(
        @AppContext context: Context
    ): Cache {
        val vaccDir = File(context.cacheDir, "vaccination")
        val cacheDir = File(vaccDir, "valueset_httpcache")
        return Cache(cacheDir, CACHE_SIZE_5MB)
    }

    @Reusable
    @Provides
    fun api(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CertificateValueSet cache: Cache
    ): CertificateValueSetApiV1 {
        val client = httpClient.newBuilder()
            .addNetworkInterceptor(CacheInterceptor())
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .build()
            .create(CertificateValueSetApiV1::class.java)
    }

    @Singleton
    @ValueSetsDataStore
    @Provides
    fun provideValueSetsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_VALUE_SETS_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_VALUE_SETS_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableValueSetsRepository(resettable: ValueSetsRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableCertificateValueSetServer(resettable: CertificateValueSetServer): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueSetsDataStore

private const val LEGACY_SHARED_PREFS_VALUE_SETS_SETTINGS_NAME = "valuesets_localdata"
private const val STORAGE_DATASTORE_VALUE_SETS_SETTINGS_NAME = "valuesets_storage"

private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L // 5MB

private class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cacheControl = CacheControl.Builder()
            .maxAge(300, TimeUnit.SECONDS)
            .build()

        // We cache as we please
        val cacheHeader = "Cache-Control"
        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader(cacheHeader)
            .addHeader(cacheHeader, cacheControl.toString())
            .build()
    }
}
