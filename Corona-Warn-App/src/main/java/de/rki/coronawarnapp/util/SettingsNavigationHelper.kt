package de.rki.coronawarnapp.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS


/**
 * A helper to navigate to the os settings, used in different places in the application,
 * e.g. settings, notification settings, tracing settings...
 */
object SettingsNavigationHelper {

    /**
     * Navigate the user to the os connection settings.
     *
     * @param context
     */
    fun toConnections(context: Context) {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        context.startActivity(intent)
    }

    /**
     * Navigate the user to the os notification settings.
     *
     * @param context
     */
    // todo has to be tested on API23 on a device
    fun toNotifications(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(
                Settings.EXTRA_APP_PACKAGE,
                context.packageName
            )
        } else {
            intent.putExtra(
                "app_package",
                context.packageName
            )
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        context.startActivity(intent)
    }

    /**
     * Navigate the user to the os exposure notification settings
     *
     * @param context
     */
    fun toEnSettings(context: Context) {
        val intent = Intent(ACTION_EXPOSURE_NOTIFICATION_SETTINGS)
        context.startActivity(intent)
    }

    // todo navigate storage settings
}
