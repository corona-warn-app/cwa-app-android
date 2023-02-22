package de.rki.coronawarnapp.eol

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object EolModule {
    @Singleton
    @Provides
    @EolSettingsDataStore
    fun provideEolSettingsDataStore(@AppContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("app_eol_dev_settings")
        }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class EolSettingsDataStore
