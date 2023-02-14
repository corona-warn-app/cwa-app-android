package de.rki.coronawarnapp.profile

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
import de.rki.coronawarnapp.profile.storage.ProfileDao
import de.rki.coronawarnapp.profile.storage.ProfileDataStore
import de.rki.coronawarnapp.profile.storage.ProfileDatabase
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.util.reset.Resettable
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ProfileModule {
    @Singleton
    @Provides
    fun familyCoronaTestDao(
        factory: ProfileDatabase.Factory
    ): ProfileDao = factory.create().profileDao()

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableProfileSettingsDataStore(resettable: ProfileSettingsDataStore): Resettable

        @Binds
        @IntoSet
        fun bindResettableProfileRepository(resettable: ProfileRepository): Resettable
    }

    @Singleton
    @ProfileDataStore
    @Provides
    fun profileDataStore(
        @ApplicationContext context: Context
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
