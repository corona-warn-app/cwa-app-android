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
 * Change the tracing text in the row based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return String
 */
fun formatTracingStatusText(tracing: Boolean, bluetooth: Boolean, location: Boolean): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_status_restricted)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_status_active)
        TracingStatusHelper.TRACING_INACTIVE, TracingStatusHelper.LOCATION ->
            appContext.getString(R.string.settings_tracing_status_inactive)
        else -> ""
    }
}

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

/**
 * Format the settings tracing content description for the header illustration
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return String
 */
fun formatTracingIllustrationText(
    tracing: Boolean,
    bluetooth: Boolean,
    location: Boolean
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_bluetooth_illustration_description_inactive)
        TracingStatusHelper.LOCATION ->
            appContext.getString(R.string.settings_tracing_location_illustration_description_inactive)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_illustration_description_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_illustration_description_inactive)
        else -> ""
    }
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


/**
 * Formats the tracing switch status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Boolean
 */
fun formatTracingSwitch(tracing: Boolean, bluetooth: Boolean, location: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.TRACING_ACTIVE -> true
        else -> false
    }
}

/**
 * Formats the tracing switch enabled status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Boolean
 */
fun formatTracingSwitchEnabled(tracing: Boolean, bluetooth: Boolean, location: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.TRACING_ACTIVE, TracingStatusHelper.TRACING_INACTIVE -> true
        else -> false
    }
}

/**
 * Formats the settings tracing details illustration depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Drawable
 */
fun formatTracingStatusImage(tracing: Boolean, bluetooth: Boolean, location: Boolean): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, location)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off)
        TracingStatusHelper.LOCATION ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_location_off)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_illustration_tracing_on)
        else ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_tracing_off)
    }
}

/**
 * Change the visibility of the bluetooth card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityBluetooth(
    tracing: Boolean,
    bluetooth: Boolean,
    location: Boolean
): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            location
        ) == TracingStatusHelper.BLUETOOTH
    )

/**
 * Change the visibility of the location card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityLocation(
    tracing: Boolean,
    bluetooth: Boolean,
    location: Boolean
): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            location
        ) == TracingStatusHelper.LOCATION
    )

/**
 * Change the visibility of the tracing text based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityTracing(
    tracing: Boolean,
    bluetooth: Boolean,
    location: Boolean
): Int {
    val tracingStatus = tracingStatusHelper(tracing, bluetooth, location)
    return formatVisibility(
        tracingStatus == TracingStatusHelper.TRACING_ACTIVE ||
                tracingStatus == TracingStatusHelper.TRACING_INACTIVE
    )
}
