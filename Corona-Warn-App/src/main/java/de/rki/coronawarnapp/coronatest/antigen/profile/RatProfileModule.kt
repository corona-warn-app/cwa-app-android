package de.rki.coronawarnapp.coronatest.antigen.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext

@Module
class RatProfileModule {

    @RatProfileDataStore
    @Provides
    fun ratProfileDataStore(
        @AppContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_NAME
            )
        )
    ) {
        context.preferencesDataStoreFile(LEGACY_SHARED_PREFS_NAME)
    }
}

private const val LEGACY_SHARED_PREFS_NAME = "ratprofile_localdata"
