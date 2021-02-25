package de.rki.coronawarnapp.storage.preferences

import javax.inject.Inject

class SharedPrefMigration @Inject constructor(
    @Named(PreferencesModule.PREFERENCES_NAME) private val settingsData: SettingsData,
    @Named(PreferencesModule.ENCRYPTED_PREFERENCES_NAME) private val encryptedSettingsData: SettingsData
) {

    fun migrate() {
        if (encryptedSettingsData.containsData()) {
            settingsData.isNotificationsRiskEnabled = encryptedSettingsData.isNotificationsRiskEnabled
            encryptedSettingsData.clear()
        }
    }
}
