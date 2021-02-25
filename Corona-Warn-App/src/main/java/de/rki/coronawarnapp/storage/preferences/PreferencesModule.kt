package de.rki.coronawarnapp.storage.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.EncryptedPreferences
import de.rki.coronawarnapp.util.di.Preferences
import de.rki.coronawarnapp.util.security.SecurityHelper

@Module
class PreferencesModule {

    @Provides
    fun provideSharedPreferences(@AppContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Provides
    @Preferences
    fun provideSettingsData(preferences: SharedPreferences): SettingsPreferences =
        SettingsPreferences(preferences)

    @Provides
    @EncryptedPreferences
    fun provideSettingsDataEncrypted(): SettingsPreferences =
        SettingsPreferences(SecurityHelper.globalEncryptedSharedPreferencesInstance)

    companion object {
        private const val SHARED_PREFERENCES_NAME = "shared_preferences"
    }
}
