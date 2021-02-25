package de.rki.coronawarnapp.storage.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import java.lang.annotation.Documented
import javax.inject.Qualifier

@Module
class PreferencesModule {

    @Provides
    fun provideSharedPreferences(@AppContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Provides
    @Named(PREFERENCES_NAME)
    fun provideSettingsData(preferences: SharedPreferences): SettingsData =
        SettingsData(preferences)

    @Provides
    @Named(ENCRYPTED_PREFERENCES_NAME)
    fun provideSettingsDataEncrypted(): SettingsData =
        SettingsData(SecurityHelper.globalEncryptedSharedPreferencesInstance)

    companion object {
        const val SHARED_PREFERENCES_NAME = "shared_preferences"
        const val ENCRYPTED_PREFERENCES_NAME = "encrypted_preferences"
        const val PREFERENCES_NAME = "preferences"
    }
}

@Qualifier
@Documented
@Retention(AnnotationRetention.RUNTIME)
annotation class Named(val value: String = "")

