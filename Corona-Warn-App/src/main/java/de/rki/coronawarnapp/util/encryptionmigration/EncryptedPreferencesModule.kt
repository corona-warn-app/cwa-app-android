package de.rki.coronawarnapp.util.encryptionmigration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class EncryptedPreferencesModule {
    @Singleton
    @EncryptionErrorResetToolDataStore
    @Provides
    fun provideEncryptionErrorResetToolDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ENCRYPTION_ERROR_RESETTOOL_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ENCRYPTION_ERROR_RESETTOOL_SETTINGS_NAME
            )
        )
    )
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class EncryptionErrorResetToolDataStore

private const val LEGACY_SHARED_PREFS_ENCRYPTION_ERROR_RESETTOOL_SETTINGS_NAME = "encryption_error_reset_tool"
private const val STORAGE_DATASTORE_ENCRYPTION_ERROR_RESETTOOL_SETTINGS_NAME = "encryption_error_reset_tool_storage"
