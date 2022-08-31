package de.rki.coronawarnapp.dccticketing.core

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.dccticketing.core.allowlist.server.DccTicketingAllowListApi1
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingApiV1
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [DccTicketingCoreModule.ResetModule::class])
object DccTicketingCoreModule {

    @Singleton
    @Provides
    @DccTicketingDataStore
    fun provideDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ) = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(DCC_TICKETING_DATASTORE_NAME) },
        migrations = LEGACY_SHARED_PREFS.map { SharedPreferencesMigration(context, it) }
    )

    @DccTicketingHttpClient
    @Provides
    fun provideHttpClient(@HttpClientDefault client: OkHttpClient): OkHttpClient = client.newBuilder().apply {
        // Remove old logger
        interceptors()
            .removeAll { it is HttpLoggingInterceptor }
            .also { Timber.tag(TAG).d("Removed old HttpLoggingInterceptor %s", it) }

        HttpLoggingInterceptor { message -> Timber.tag(TAG).v(message) }
            .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            .also { addInterceptor(it) }
    }.build()

    @Reusable
    @Provides
    fun provideDccTicketingValidationApiV1(
        @DccTicketingHttpClient client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccTicketingApiV1 = Retrofit.Builder()
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .build()
        .create(DccTicketingApiV1::class.java)

    @Reusable
    @Provides
    fun allowListApi(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @DccTicketing cache: Cache
    ): DccTicketingAllowListApi1 = Retrofit.Builder()
        .client(
            httpClient.newBuilder()
                .cache(cache)
                .build()
        )
        .baseUrl(url)
        .build()
        .create(DccTicketingAllowListApi1::class.java)

    @Singleton
    @Provides
    @DccTicketing
    // Create in files dir to be in line with tech spec
    fun localStorageDir(
        @AppContext context: Context
    ): File = File(context.filesDir, "dcc_ticketing_local_storage")

    @Singleton
    @Provides
    @DccTicketing
    fun httpCache(
        @AppContext context: Context
    ): Cache = Cache(File(context.cacheDir, "dcc_ticketing"), DEFAULT_CACHE_SIZE)

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDccTicketingAllowListRepository(resettable: DccTicketingAllowListRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccTicketingAllowListStorage(resettable: DccTicketingAllowListStorage): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccTicketingQrCodeSettings(resettable: DccTicketingQrCodeSettings): Resettable
    }
}

private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB

// Dummy base url to satisfy Retrofit ¯\_(ツ)_/¯
private const val BASE_URL = "https://localhost"

private const val TAG = "DccTicketingOkHttpClient"

private const val DCC_TICKETING_DATASTORE_NAME = "dcc_ticketing_localdata"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccTicketingDataStore

private val LEGACY_SHARED_PREFS
    get() = listOf("dccTicketing_qrcode")
