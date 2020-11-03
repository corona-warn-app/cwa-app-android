package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R

data class SettingsNotificationState(
    val isNotificationsEnabled: Boolean,
    val isNotificationsRiskEnabled: Boolean,
    val isNotificationsTestEnabled: Boolean
) {

    val isEnabled: Boolean
        get() = (isNotificationsEnabled &&
            (isNotificationsRiskEnabled || isNotificationsTestEnabled))

    /**
     * Formats the settings icon color for notifications depending on notification values
     */
    @ColorInt
    fun getNotificationIconColor(c: Context): Int = c.getColor(
        if (isEnabled) R.color.colorAccentTintIcon else R.color.colorTextSemanticRed
    )

    /**
     * Formats settings icon color for notifications depending on notification values
     */
    fun getNotificationIcon(c: Context): Drawable? = c.getDrawable(
        if (isEnabled) R.drawable.ic_settings_notification_active
        else R.drawable.ic_settings_notification_inactive
    )

    /**
     * Formats the text display of settings notification status depending on notification values
     */
    fun getNotificationStatusText(c: Context): String = c.getString(
        if (isEnabled) R.string.settings_on else R.string.settings_off
    )
}
