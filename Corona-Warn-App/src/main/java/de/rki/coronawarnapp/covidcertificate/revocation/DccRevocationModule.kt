package de.rki.coronawarnapp.covidcertificate.revocation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.covidcertificate.revocation.server.DccRevocationApi
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [DccRevocationModule.ResetModule::class])
object DccRevocationModule {

    @Singleton
    @DccRevocationCache
    @Provides
    fun provideCache(@AppContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        return Cache(cacheDir, CACHE_SIZE)
    }

    @Provides
    fun provideRevocationApi(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @DccRevocationCache cache: Cache
    ): DccRevocationApi {
        val revocationClient = httpClient.newBuilder().cache(cache).build()
        return Retrofit.Builder()
            .client(revocationClient)
            .baseUrl(url)
            .build()
            .create(DccRevocationApi::class.java)
    }

    @Singleton
    @Provides
    @DccRevocationDataStore
    fun provideRevocationDataStore(
        @AppContext context: Context,
        @AppScope scope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(REVOCATION_DATASTORE_NAME) }
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDccRevocationReset(resettable: DccRevocationReset): Resettable
    }
}

private const val CACHE_DIR = "revocation_http_cache"
private const val CACHE_SIZE = 50 * 1024 * 1024L // 50MB

private const val REVOCATION_DATASTORE_NAME = "revocation_localdata"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccRevocationDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccRevocationCache
