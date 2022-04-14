package de.rki.coronawarnapp.covidcertificate.revocation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationApi
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object RevocationModule {

    @Singleton
    @RevocationCache
    @Provides
    fun provideCache(@AppContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        return Cache(cacheDir, CACHE_SIZE)
    }

    @Provides
    fun provideRevocationApi(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @RevocationCache cache: Cache
    ): RevocationApi {
        val revocationClient = httpClient.newBuilder().cache(cache).build()
        return Retrofit.Builder()
            .client(revocationClient)
            .baseUrl(url)
            .build()
            .create(RevocationApi::class.java)
    }

    @Singleton
    @Provides
    @RevocationDataStore
    fun provideRevocationDataStore(
        @AppContext context: Context,
        @AppScope scope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(REVOCATION_DATASTORE_NAME) }
    )
}

private const val CACHE_DIR = "revocation_http_cache"
private const val CACHE_SIZE = 50 * 1024 * 1024L // 50MB

private const val REVOCATION_DATASTORE_NAME = "revocation_localdata"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RevocationDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RevocationCache
