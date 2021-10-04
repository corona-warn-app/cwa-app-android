package de.rki.coronawarnapp.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// TODO: will be refactored in EXPOSUREAPP-9618
@Singleton
class NotificationSettings @Inject constructor(
    foregroundState: ForegroundState,
    private val cwaSettings: CWASettings,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    val isNotificationsEnabled: Flow<Boolean> = foregroundState.isInForeground.map {
        // We ignore the foreground state
        // We just want to know when the user could have toggled notifications
        notificationManagerCompat.areNotificationsEnabled()
    }

    val isNotificationsRiskEnabled: Flow<Boolean> = cwaSettings.isNotificationsRiskEnabled.flow

    /**
     * Toggle notifications risk updates.
     *
     */
    fun toggleNotificationsRiskEnabled() {
        cwaSettings.isNotificationsRiskEnabled.update { !it }
    }

    val isNotificationsTestEnabled: Flow<Boolean> = cwaSettings.isNotificationsTestEnabled.flow

    /**
     * Toggle notifications for test updates in shared preferences and refresh it afterwards.
     *
     */
    fun toggleNotificationsTestEnabled() {
        cwaSettings.isNotificationsTestEnabled.update { !it }
    }
}
