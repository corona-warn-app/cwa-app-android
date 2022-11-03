package de.rki.coronawarnapp.srs.core

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
object SrsSubmissionModule {

    @Singleton
    @Provides
    @SrsSettingsDataStore
    fun provideSrsSettingsDataStore(@AppContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("srs_settings_localdata")
        }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class SrsSettingsDataStore
