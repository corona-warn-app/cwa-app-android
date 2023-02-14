package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.UploadHistory
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UploadHistoryStorageModule {

    @Singleton
    @Provides
    fun provideDataStore(
        serializer: UploadHistorySerializer,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
        @ApplicationContext context: Context,
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
        @ApplicationContext context: Context,
        @BaseJackson mapper: ObjectMapper
    ) = SharedPreferencesMigration<UploadHistory>(
        context = context,
        sharedPreferencesName = LEGACY_SHARED_PREFS
    ) { sharedPreferencesView, uploadHistory ->
        val migratedUploadHistory = runCatching {
            // Data was converted with Gson before so use Gson to restore data to avoid corrupted data
            // Gson and Jackson store Instants differently
            sharedPreferencesView.getString(LEGACY_UPLOAD_HISTORY_KEY)?.let { mapper.readValue<UploadHistory>(it) }
        }
            .onFailure { Timber.tag("SharedPreferencesMigration<UploadHistory>").e(it, "Migration failed") }
            .getOrNull()

        migratedUploadHistory ?: uploadHistory
    }

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableUploadHistoryStorage(resettable: UploadHistoryStorage): Resettable
    }
}

private const val UPLOAD_HISTORY_DATA_STORE: String = "upload_history_data_store"
private const val LEGACY_SHARED_PREFS = "bugreporting_localdata"
@VisibleForTesting const val LEGACY_UPLOAD_HISTORY_KEY = "upload.history"
