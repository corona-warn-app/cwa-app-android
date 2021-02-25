package de.rki.coronawarnapp.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.preferences.Named
import de.rki.coronawarnapp.storage.preferences.PreferencesModule.Companion.PREFERENCES_NAME
import de.rki.coronawarnapp.storage.preferences.SettingsData
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettings @Inject constructor(
    foregroundState: ForegroundState,
    @Named(PREFERENCES_NAME) private val settingsData: SettingsData,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    val isNotificationsEnabled: Flow<Boolean> = foregroundState.isInForeground.map {
        // We ignore the foreground state
        // We just want to know when the user could have toggled notifications
        notificationManagerCompat.areNotificationsEnabled()
    }

    val isNotificationsRiskEnabled: Flow<Boolean> = settingsData.isNotificationsRiskEnabledFlow

    /**
     * Toggle notifications risk updates.
     *
     * @see LocalData
     */
    fun toggleNotificationsRiskEnabled() {
        settingsData.isNotificationsRiskEnabled = !settingsData.isNotificationsRiskEnabled
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
