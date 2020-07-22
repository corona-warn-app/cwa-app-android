@file:JvmName("FormatterSettingsHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

/*Texter*/

/**
 * Formats the text display of settings item status depending on flag provided
 *
 * @param value
 * @return String
 */
fun formatStatus(value: Boolean): String = formatText(
    value,
    R.string.settings_on,
    R.string.settings_off
)

/**
 * Formats the text display of settings notification status depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return
 */
fun formatNotificationsStatusText(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): String =
    formatStatus((notifications && (notificationsRisk || notificationsTest)))

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
 * @param connection
 * @param location
 * @return String
 */
fun formatTracingStatusText(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION, TracingStatusHelper.BLUETOOTH ->
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
 * Format the settings tracing description text display depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return String
 */
fun formatTracingDescription(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION ->
            appContext.getString(R.string.settings_tracing_body_connection_inactive)
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_body_bluetooth_inactive)
        TracingStatusHelper.LOCATION ->
            appContext.getString(R.string.settings_tracing_body_inactive_location)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_body_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_body_inactive)
        else -> ""
    }
}

/**
 * Format the settings tracing content description text display depending on tracing status
 * but appends the word button at the end for screen reader accessibility reasons
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return String
 */
fun formatTracingContentDescription(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION ->
            appContext.getString(R.string.settings_tracing_body_connection_inactive) +
                    " " + appContext.getString(R.string.accessibility_button)
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_body_bluetooth_inactive) +
                    " " + appContext.getString(R.string.accessibility_button)
        TracingStatusHelper.LOCATION ->
            appContext.getString(R.string.settings_tracing_body_inactive_location) +
                    " " + appContext.getString(R.string.accessibility_button)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_body_active) +
                    " " + appContext.getString(R.string.accessibility_button)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_body_inactive) +
                    " " + appContext.getString(R.string.accessibility_button)
        else -> ""
    }
}

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
 * @param connection
 * @param location
 * @return String
 */
fun formatTracingIllustrationText(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION ->
            appContext.getString(R.string.settings_tracing_connection_illustration_description_inactive)
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
 * Formats the settings icon color for notifications depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return Int
 */
fun formatNotificationIconColor(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): Int =
    formatColor(
        (notifications && (notificationsRisk || notificationsTest)),
        R.color.colorAccentTintIcon,
        R.color.colorTextSemanticRed
    )

/**
 * Formats settings icon color for notifications depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return
 */
fun formatNotificationIcon(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): Drawable? =
    formatDrawable(
        (notifications && (notificationsRisk || notificationsTest)),
        R.drawable.ic_settings_notification_active,
        R.drawable.ic_settings_notification_inactive
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
        R.drawable.ic_illustration_notification_on,
        R.drawable.ic_settings_illustration_notification_off
    )

/**
 * Formats the settings icon color for tracing depending on tracing values
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return
 */
fun formatSettingsTracingIconColor(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION, TracingStatusHelper.BLUETOOTH ->
            appContext.getColor(R.color.colorTextPrimary3)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getColor(R.color.colorAccentTintIcon)
        TracingStatusHelper.TRACING_INACTIVE, TracingStatusHelper.LOCATION ->
            appContext.getColor(R.color.colorTextSemanticRed)
        else -> appContext.getColor(R.color.colorTextSemanticRed)
    }
}

/**
 * Formats the settings icon for tracing depending on tracing values
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return
 */
fun formatSettingsTracingIcon(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.CONNECTION,
        TracingStatusHelper.BLUETOOTH,
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_active_small)
        TracingStatusHelper.LOCATION ->
            appContext.getDrawable(R.drawable.ic_settings_location_inactive_small)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_inactive_small)
        else -> appContext.getDrawable(R.drawable.ic_settings_tracing_inactive_small)
    }
}

/**
 * Formats the settings icon for background priority
 */
fun formatSettingsBackgroundPriorityIcon(
    enabled: Boolean
): Drawable? = formatDrawable(
    enabled,
    R.drawable.ic_settings_background_priority_enabled,
    R.drawable.ic_settings_background_priority_disabled
)

/**
 * Formats the settings icon color for background priority
 */
fun formatSettingsBackgroundPriorityIconColor(
    enabled: Boolean
): Int =
    formatColor(
        enabled,
        R.color.colorAccentTintIcon,
        R.color.colorTextSemanticRed
    )

/**
 * Formats the tracing switch status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Boolean
 */
fun formatTracingSwitch(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.TRACING_ACTIVE -> true
        else -> false
    }
}

/**
 * Formats the tracing switch enabled status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Boolean
 */
fun formatTracingSwitchEnabled(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.TRACING_ACTIVE, TracingStatusHelper.TRACING_INACTIVE -> true
        else -> false
    }
}

/**
 * Formats the main tracing icon depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Drawable
 */
fun formatTracingIcon(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Int? {
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.BLUETOOTH -> R.drawable.ic_settings_tracing_bluetooth_inactive
        TracingStatusHelper.CONNECTION -> R.drawable.ic_settings_tracing_connection_inactive
        TracingStatusHelper.LOCATION -> R.drawable.ic_settings_location_inactive_small
        TracingStatusHelper.TRACING_ACTIVE -> R.raw.ic_settings_tracing_animated
        else -> R.drawable.ic_settings_tracing_inactive
    }
}

/**
 * Formats the main tracing icon color depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Int
 */
fun formatTracingIconColor(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getColor(R.color.colorAccentTintIcon)
        else ->
            appContext.getColor(R.color.colorTextSemanticRed)
    }
}

/**
 * Formats the settings tracing details illustration depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Drawable
 */
fun formatTracingStatusImage(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection, location)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off)
        TracingStatusHelper.CONNECTION ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_connection_off)
        TracingStatusHelper.LOCATION ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_location_off)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_illustration_tracing_on)
        else ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_tracing_off)
    }
}

/**
 * Change the visibility of the connection card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Int
 */
fun formatTracingStatusConnection(tracing: Boolean, bluetooth: Boolean, connection: Boolean, location: Boolean): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            connection,
            location
        ) == TracingStatusHelper.CONNECTION
    )

/**
 * Change the visibility of the bluetooth card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityBluetooth(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            connection,
            location
        ) == TracingStatusHelper.BLUETOOTH
    )

/**
 * Change the visibility of the location card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityLocation(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            connection,
            location
        ) == TracingStatusHelper.LOCATION
    )

/**
 * Change the visibility of the tracing text based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @param location
 * @return Int
 */
fun formatTracingStatusVisibilityTracing(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean,
    location: Boolean
): Int {
    val tracingStatus = tracingStatusHelper(tracing, bluetooth, connection, location)
    return formatVisibility(
        tracingStatus == TracingStatusHelper.TRACING_ACTIVE ||
                tracingStatus == TracingStatusHelper.TRACING_INACTIVE
    )
}
