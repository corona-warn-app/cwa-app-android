package de.rki.coronawarnapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExternalActionException
import de.rki.coronawarnapp.exception.reporting.report

/**
 * Helper object for external actions
 *
 */
object ExternalActionHelper {
    private val TAG: String? = ExternalActionHelper::class.simpleName

    /**
     * Opens the share default overlay to provide the Corona-Warn-App installation link
     *
     * @param fragment
     * @param text
     * @param title
     */
    fun shareText(fragment: Fragment, text: String, title: String?) {
        try {
            fragment.startActivity(Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }, title))
        } catch (exception: Exception) {
            // catch generic exception on share
            // possibly due to bad share content format
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Opens the client default phone app and inserts a given number
     *
     * @param fragment
     * @param uri
     */
    fun call(fragment: Fragment, uri: String) {
        try {
            fragment.startActivity(
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$uri")
                )
            )
        } catch (exception: Exception) {
            // catch generic exception on call
            // possibly due to bad number format
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Opens a given url in the client default browser
     *
     * @param fragment
     * @param url
     */
    fun openUrl(fragment: Fragment, url: String) {
        try {
            fragment.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (exception: Exception) {
            // catch generic exception on url navigation
            // most likely due to bad url format
            // or less likely no browser installed
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Navigate the user to the os connection settings.
     *
     * @param context
     */
    fun toConnections(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Navigate the user to the os notification settings.
     *
     * @param context
     */
    fun toNotifications(context: Context) {
        try {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    context.packageName
                )
            } else {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra(
                    "app_package",
                    context.packageName
                )
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Navigate the user to the os settings as navigation to
     * bluetooth settings directly is not reliable for all devices
     *
     * @param context
     */
    fun toMainSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    fun disableBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + context.packageName)
            )
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    fun toBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    fun toBatterySaverSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            context.startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }
}
