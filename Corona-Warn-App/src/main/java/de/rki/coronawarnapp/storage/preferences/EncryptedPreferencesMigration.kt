package de.rki.coronawarnapp.storage.preferences

import de.rki.coronawarnapp.util.di.EncryptedPreferences
import de.rki.coronawarnapp.util.di.Preferences
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    @Preferences private val settingsPreferences: SettingsPreferences,
    @EncryptedPreferences private val encryptedSettingsPreferences: SettingsPreferences
) {

    fun migrate() {
        Timber.d("EncryptedPreferencesMigration START")
        if (encryptedSettingsPreferences.containsData()) {
            Timber.d("EncryptedPreferencesMigration encryptedSettingsData")
            settingsPreferences.isNotificationsRiskEnabled = encryptedSettingsPreferences.isNotificationsRiskEnabled
            encryptedSettingsPreferences.clear()
        }
        Timber.d("EncryptedPreferencesMigration END")
    }
}
