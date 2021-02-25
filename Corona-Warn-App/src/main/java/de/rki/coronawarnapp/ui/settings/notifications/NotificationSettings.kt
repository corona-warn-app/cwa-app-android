package de.rki.coronawarnapp.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.preferences.SettingsPreferences
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettings @Inject constructor(
    foregroundState: ForegroundState,
    @Preferences private val settingsPreferences: SettingsPreferences,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    val isNotificationsEnabled: Flow<Boolean> = foregroundState.isInForeground.map {
        // We ignore the foreground state
        // We just want to know when the user could have toggled notifications
        notificationManagerCompat.areNotificationsEnabled()
    }

    val isNotificationsRiskEnabled: Flow<Boolean> = settingsPreferences.isNotificationsRiskEnabledFlow

    /**
     * Toggle notifications risk updates.
     *
     * @see LocalData
     */
    fun toggleNotificationsRiskEnabled() {
        settingsPreferences.isNotificationsRiskEnabled = !settingsPreferences.isNotificationsRiskEnabled
    }

    val isNotificationsTestEnabled: Flow<Boolean> = LocalData.isNotificationsTestEnabledFlow

    /**
     * Toggle notifications for test updates in shared preferences and refresh it afterwards.
     *
     * @see LocalData
     */
    fun toggleNotificationsTestEnabled() {
        LocalData.isNotificationsTestEnabled = !LocalData.isNotificationsTestEnabled
    }
}
