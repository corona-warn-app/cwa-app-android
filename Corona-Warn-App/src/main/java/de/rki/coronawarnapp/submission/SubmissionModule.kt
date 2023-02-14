package de.rki.coronawarnapp.submission

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
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.environment.submission.SubmissionCDNServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.RestrictedConnectionSpecs
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.server.SubmissionApiV1
import de.rki.coronawarnapp.submission.server.SubmissionHttpClient
import de.rki.coronawarnapp.submission.task.DefaultKeyConverter
import de.rki.coronawarnapp.submission.task.KeyConverter
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SubmissionModule {

    @Reusable
    @SubmissionHttpClient
    @Provides
    fun cdnHttpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient,
        @RestrictedConnectionSpecs connectionSpecs: List<ConnectionSpec>
    ): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(connectionSpecs).build()

    @Singleton
    @Provides
    fun provideSubmissionApi(
        @ApplicationContext context: Context,
        @SubmissionHttpClient client: OkHttpClient,
        @SubmissionCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        jacksonConverterFactory: JacksonConverterFactory
    ): SubmissionApiV1 {
        val cache = Cache(File(context.cacheDir, "http_submission"), DEFAULT_CACHE_SIZE)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(jacksonConverterFactory)
            .build()
            .create(SubmissionApiV1::class.java)
    }

    @Singleton
    @SubmissionSettingsDataStore
    @Provides
    fun provideSubmissionSettingsDataStore(
        @ApplicationContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_SUBMISSION_SETTINGS_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_SUBMISSION_SETTINGS_SETTINGS_NAME
            )
        )
    )

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableSubmissionSettings(resettable: SubmissionSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableTEKHistoryStorage(resettable: TEKHistoryStorage): Resettable
    }

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface BindsModule {

        @Binds
        fun provideKeyConverter(defaultKeyConverter: DefaultKeyConverter): KeyConverter
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class SubmissionSettingsDataStore

private const val LEGACY_SHARED_PREFS_SUBMISSION_SETTINGS_SETTINGS_NAME = "submission_localdata"
private const val STORAGE_DATASTORE_SUBMISSION_SETTINGS_SETTINGS_NAME = "submission_settings_storage"

const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
