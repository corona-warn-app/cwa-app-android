package de.rki.coronawarnapp.diagnosiskeys

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyApiV1
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [DiagnosisKeysModule.ResetModule::class])
object DiagnosisKeysModule {

    @Singleton
    @Provides
    fun provideDiagnosisKeyApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): DiagnosisKeyApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DiagnosisKeyApiV1::class.java)

    @Singleton
    @DownloadDiagnosisKeysSettingsDataStore
    @Provides
    fun provideDownloadDiagnosisKeysSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_DOWNLOAD_DIAGNOSIS_KEYS_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_DOWNLOAD_DIAGNOSIS_KEYS_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableKeyCacheRepository(resettable: KeyCacheRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableDownloadDiagnosisKeysSettings(resettable: DownloadDiagnosisKeysSettings): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DownloadDiagnosisKeysSettingsDataStore

private const val LEGACY_SHARED_PREFS_DOWNLOAD_DIAGNOSIS_KEYS_SETTINGS_NAME = "keysync_localdata"
private const val STORAGE_DATASTORE_DOWNLOAD_DIAGNOSIS_KEYS_SETTINGS_NAME =
    "download_diagnosis_keys_settings_storage"
