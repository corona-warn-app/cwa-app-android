package de.rki.coronawarnapp.statistics

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsApiV1
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsCache
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsServer
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.source.StatisticsApiV1
import de.rki.coronawarnapp.statistics.source.StatisticsCache
import de.rki.coronawarnapp.statistics.source.StatisticsServer
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@Module(includes = [StatisticsModule.ResetModule::class])
object StatisticsModule {

    @Singleton
    @Provides
    @Statistics
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "statistics")

    @Singleton
    @Provides
    @Statistics
    fun httpCache(
        @Statistics cacheDir: File
    ): Cache = Cache(File(cacheDir, "cache_http"), DEFAULT_CACHE_SIZE)

    @Singleton
    @Provides
    fun api(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
        @Statistics cache: Cache
    ): StatisticsApiV1 {
        val configHttpClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(StatisticsApiV1::class.java)
    }

    @Singleton
    @Provides
    fun localApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
        @Statistics cache: Cache
    ): LocalStatisticsApiV1 {
        val configHttpClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(LocalStatisticsApiV1::class.java)
    }

    @Singleton
    @LocalStatisticsConfigDataStore
    @Provides
    fun provideLocalStatisticsConfigDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_LOCAL_STATISTICS_CONFIG_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_LOCAL_STATISTICS_CONFIG_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableStatisticsCache(resettable: StatisticsCache): Resettable

        @Binds
        @IntoSet
        fun bindResettableStatisticsServer(resettable: StatisticsServer): Resettable

        @Binds
        @IntoSet
        fun bindResettableLocalStatisticsConfigStorage(resettable: LocalStatisticsConfigStorage): Resettable

        @Binds
        @IntoSet
        fun bindResettableLocalStatisticsServer(resettable: LocalStatisticsServer): Resettable

        @Binds
        @IntoSet
        fun bindResettableLocalStatisticsCache(resettable: LocalStatisticsCache): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalStatisticsConfigDataStore

private const val LEGACY_SHARED_PREFS_LOCAL_STATISTICS_CONFIG_SETTINGS_NAME = "statistics_local_config"
private const val STORAGE_DATASTORE_LOCAL_STATISTICS_CONFIG_SETTINGS_NAME = "local_statistics_config_storage"

private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
private val HTTP_TIMEOUT = Duration.ofSeconds(10)

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Statistics
