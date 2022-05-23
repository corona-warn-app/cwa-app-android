package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Singleton

@Module
object UploadHistoryStorageModule {

    @Singleton
    @Provides
    fun provideDataStore(
        serializer: UploadHistorySerializer,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
        @AppContext context: Context,
        migration: SharedPreferencesMigration<UploadHistory>
    ): DataStore<UploadHistory> = DataStoreFactory.create(
        serializer = serializer,
        scope = appScope + dispatcherProvider.IO,
        migrations = listOf(migration)
    ) {
        context.dataStoreFile(UPLOAD_HISTORY_DATA_STORE)
    }

    @Provides
    fun provideMigration(
        @AppContext context: Context,
        serializer: UploadHistorySerializer
    ) = SharedPreferencesMigration<UploadHistory>(
        context = context,
        sharedPreferencesName = LEGACY_SHARED_PREFS
    ) { sharedPreferencesView, uploadHistory ->
        Timber.e("uploadHistory=%s", uploadHistory)
        val json = sharedPreferencesView.getString(Key)
        when(json == null) {
            true -> serializer.defaultValue
            false -> json.byteInputStream().use { serializer.readFrom(it) }
        }
    }
}

private const val UPLOAD_HISTORY_DATA_STORE: String = "upload_history_data_store"
private const val LEGACY_SHARED_PREFS = "bugreporting_localdata"
private const val Key = "upload.history"
