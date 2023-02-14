package de.rki.coronawarnapp.main

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [MainModule.CwaSettingsDataStoreModule::class])
interface MainModule {

    @Binds
    @IntoSet
    fun bindResettableCWASettings(resettable: CWASettings): Resettable

    @Module
    @InstallIn(SingletonComponent::class)
    object CwaSettingsDataStoreModule {
        @Singleton
        @CwaSettingsDataStore
        @Provides
        fun provideCwaSettingsDataStore(
            @ApplicationContext context: Context,
            @AppScope appScope: CoroutineScope,
            dispatcherProvider: DispatcherProvider
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = appScope + dispatcherProvider.IO,
            produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_CWA_SETTINGS_NAME) },
            migrations = listOf(SharedPreferencesMigration(context, LEGACY_SHARED_PREFS_CWA_SETTINGS_NAME))
        )
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CwaSettingsDataStore

private const val LEGACY_SHARED_PREFS_CWA_SETTINGS_NAME = "cwa_main_localdata"
private const val STORAGE_DATASTORE_CWA_SETTINGS_NAME = "cwa_settings_storage"
