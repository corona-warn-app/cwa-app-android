package de.rki.coronawarnapp.srs.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.srs.core.storage.DefaultSrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SrsDevSettingsModule {
    @Binds
    abstract fun srsDevSettings(settings: DefaultSrsDevSettings): SrsDevSettings

    companion object {
        @Singleton
        @Provides
        @SrsDevSettingsDataStore
        fun provideSrsDevSettingsStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile("srs_dev_settings_localdata")
            }
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class SrsDevSettingsDataStore
