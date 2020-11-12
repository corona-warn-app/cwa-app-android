package de.rki.coronawarnapp.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ForegroundState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettings @Inject constructor(
    foregroundState: ForegroundState,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    val isNotificationsEnabled: Flow<Boolean> = foregroundState.isInForeground.map {
        // We ignore the foreground state
        // We just want to know when the user could have toggled notifications
        notificationManagerCompat.areNotificationsEnabled()
    }

    val isNotificationsRiskEnabled: Flow<Boolean> = LocalData.isNotificationsRiskEnabledFlow

    /**
     * Toggle notifications risk updates.
     *
     * @see LocalData
     */
    fun toggleNotificationsRiskEnabled() {
        LocalData.isNotificationsRiskEnabled = !LocalData.isNotificationsRiskEnabled
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
