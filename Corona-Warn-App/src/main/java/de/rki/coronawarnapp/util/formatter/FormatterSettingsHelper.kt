@file:JvmName("FormatterSettingsHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R

/*Texter*/
/**
 * Formats the text display of settings item status depending on flag provided
 *
 * @param value
 * @return
 */
fun formatStatus(value: Boolean): String = formatText(
    value,
    R.string.settings_on,
    R.string.settings_off
)

/**
 * Formats the main text display of tracing status depending on tracing status
 *
 * @param tracing
 * @return
 */
fun formatTracingText(tracing: Boolean): String = formatText(
    tracing,
    R.string.settings_tracing_body_active,
    R.string.settings_tracing_body_inactive
)

/**
 * Format the settings tracing description text display depending on tracing status
 *
 * @param tracing
 * @return
 */
fun formatTracingDescription(tracing: Boolean): String = formatText(
    tracing,
    R.string.settings_tracing_body_active_description,
    R.string.settings_tracing_body_inactive_description
)

/**
 * Formats the settings notifications title display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsTitle(notifications: Boolean): String = formatText(
    notifications,
    R.string.settings_notifications_headline_active,
    R.string.settings_notifications_headline_inactive
)

/**
 * Formats the settings notifications description text display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsDescription(notifications: Boolean): String = formatText(
    notifications,
    R.string.settings_notifications_body_active,
    R.string.settings_notifications_body_inactive
)

/*Styler*/
/**
 * Formats the settings icon color depending on flag provided
 *
 * @param active
 * @return
 */
fun formatIconColor(active: Boolean): Int =
    formatColor(active, R.color.settingsIconActive, R.color.settingsIconInactive)

/**
 * Formats the main tracing icon depending on tracing status
 *
 * @param tracing
 * @return
 */
fun formatTracingIcon(tracing: Boolean): Drawable? =
    formatDrawable(tracing, R.drawable.ic_tracing_on, R.drawable.ic_tracing_off)

/**
 * Formats the main tracing icon color depending on tracing status
 *
 * @param tracing
 * @return
 */
fun formatTracingIconColor(tracing: Boolean): Int =
    formatColor(tracing, R.color.tracingIconActive, R.color.tracingIconInactive)

/**
 * Formats the settings tracing details illustration depending on tracing status
 *
 * @param tracing
 * @return
 */
fun formatTracingImage(tracing: Boolean): Drawable? =
    formatDrawable(
        tracing,
        R.drawable.ic_settings_illustration_tracing_on,
        R.drawable.ic_settings_illustration_tracing_off
    )

/**
 * Formats the settings notifications details illustration depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationImage(notifications: Boolean): Drawable? =
    formatDrawable(
        notifications,
        R.drawable.ic_settings_illustration_notification_on,
        R.drawable.ic_settings_illustration_notification_off
    )
