package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Singleton

@Module(includes = [UploadHistoryStorageModule.ResetModule::class])
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
        @BaseGson gson: Gson
    ) = SharedPreferencesMigration<UploadHistory>(
        context = context,
        sharedPreferencesName = LEGACY_SHARED_PREFS
    ) { sharedPreferencesView, uploadHistory ->
        Timber.e("uploadHistory=%s", uploadHistory)
        val migratedUploadHistory = runCatching {
            // Data was converted with Gson before so use Gson to restore data to avoid corrupted data
            // Gson and Jackson store Instants differently
            sharedPreferencesView.getString(LEGACY_UPLOAD_HISTORY_KEY)?.let { gson.fromJson<UploadHistory>(it) }
        }.getOrNull()

        migratedUploadHistory ?: uploadHistory
    }

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableUploadHistoryStorage(resettable: UploadHistoryStorage): Resettable
    }
}

private const val UPLOAD_HISTORY_DATA_STORE: String = "upload_history_data_store"
private const val LEGACY_SHARED_PREFS = "bugreporting_localdata"
private const val LEGACY_UPLOAD_HISTORY_KEY = "upload.history"
