package de.rki.coronawarnapp.ui.settings.notifications

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R

data class NotificationSettingsState(
    val isNotificationsEnabled: Boolean,
    val isNotificationsRiskEnabled: Boolean,
    val isNotificationsTestEnabled: Boolean
) {

    /**
     * Formats the settings notifications description text display depending on notifications status
     */
    fun getNotificationsDescription(c: Context): String? = if (isNotificationsEnabled) {
        c.getString(R.string.settings_notifications_body_active)
    } else {
        null
    }

    /**
     * Formats the settings notifications title display depending on notifications status
     */
    fun getNotificationsTitle(c: Context): String? = if (isNotificationsEnabled) {
        c.getString(R.string.settings_notifications_headline_active)
    } else {
        null
    }

    /**
     * Formats the settings notifications details illustration depending on notifications status
     */
    fun getNotificationsImage(c: Context): Drawable? = c.getDrawable(
        if (isNotificationsEnabled) R.drawable.ic_illustration_notification_on
        else R.drawable.ic_settings_illustration_notification_off
    )

    /**
     * Formats the settings notifications details illustration description depending on notifications status
     */
    fun getNotificationsIllustrationText(c: Context): String = c.getString(
        if (isNotificationsEnabled) R.string.settings_notifications_illustration_description_active
        else R.string.settings_notifications_illustration_description_inactive
    )

    fun getRiskNotificationStatusText(c: Context): String = c.getString(
        if (isNotificationsRiskEnabled) R.string.settings_on
        else R.string.settings_off
    )

    fun getTestNotificationStatusText(c: Context): String = c.getString(
        if (isNotificationsTestEnabled) R.string.settings_on
        else R.string.settings_off
    )

    fun isNotificationCardVisible() = !isNotificationsEnabled
}
