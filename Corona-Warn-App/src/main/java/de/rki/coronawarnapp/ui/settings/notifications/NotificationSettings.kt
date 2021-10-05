package de.rki.coronawarnapp.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.util.device.ForegroundState
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
}
