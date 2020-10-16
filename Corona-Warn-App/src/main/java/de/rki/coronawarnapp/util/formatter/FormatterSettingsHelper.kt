@file:JvmName("FormatterSettingsHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

/*Texter*/


fun formatStatus(value: Boolean): String = formatText(
    value,
    R.string.settings_on,
    R.string.settings_off
)


/**
 * Formats the settings notifications title display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsTitle(notifications: Boolean): String? =
    formatText(notifications, R.string.settings_notifications_headline_active, null)

/**
 * Formats the settings notifications description text display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsDescription(notifications: Boolean): String? =
    formatText(notifications, R.string.settings_notifications_body_active, null)

/**
 * Formats the settings notifications details illustration description depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationIllustrationText(notifications: Boolean): String =
    formatText(
        notifications,
        R.string.settings_notifications_illustration_description_active,
        R.string.settings_notifications_illustration_description_inactive
    )

/**
 * Returns a combined string of subtitle and status for the content description for switches
 *
 * @param subtitle
 * @param status
 * @return String
 */
fun formatSwitchContentDescription(subtitle: String, status: String): String = "$subtitle $status"



/**
 * Formats the tracing body depending on the tracing status and the days since last exposure.
 *
 * @param activeTracingDaysInRetentionPeriod
 * @return String
 */
// TODO add generic plural formatter helper
fun formatTracingStatusBody(activeTracingDaysInRetentionPeriod: Long): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val resources = appContext.resources
    val days = activeTracingDaysInRetentionPeriod.toInt()
    return resources.getQuantityString(R.plurals.settings_tracing_status_body_active, days, days)
}

/*Styler*/
/**
 * Formats the settings icon color depending on flag provided
 *
 * @param active
 * @return Int
 */
fun formatIconColor(active: Boolean): Int =
    formatColor(active, R.color.colorAccentTintIcon, R.color.colorTextPrimary3)

/**
 * Formats the settings notifications details illustration depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationImage(notifications: Boolean): Drawable? =
    formatDrawable(
        notifications,
        R.drawable.ic_illustration_notification_on,
        R.drawable.ic_settings_illustration_notification_off
    )

fun formatTracingSwitch(tracing: Boolean, bluetooth: Boolean, location: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.TRACING_ACTIVE -> true
        else -> false
    }
}
