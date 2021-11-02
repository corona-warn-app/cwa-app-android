package de.rki.coronawarnapp.ui.settings.notifications

import android.content.Context
import de.rki.coronawarnapp.R

data class NotificationSettingsState(val isNotificationsEnabled: Boolean) {
    /**
     * Formats the settings notifications title display depending on notifications status
     */
    fun getNotificationsHeader(): Int = if (isNotificationsEnabled) {
        R.string.nm_notification_settings
    } else {
        R.string.nm_notification_enabled
    }

    /**
     * Formats the settings notifications details illustration depending on notifications status
     */
    fun getNotificationsImage(): Int = if (isNotificationsEnabled)
        R.drawable.ic_illustration_notification_on
    else
        R.drawable.ic_settings_illustration_notification_off

    /**
     * Formats the settings notifications details illustration description depending on notifications status
     */
    fun getNotificationsIllustrationText(c: Context): String = c.getString(
        if (isNotificationsEnabled) R.string.settings_notifications_illustration_description_active
        else R.string.settings_notifications_illustration_description_inactive
    )

    fun getNotificationStatusText(): Int = if (isNotificationsEnabled)
        R.string.settings_on
    else
        R.string.settings_off
}
