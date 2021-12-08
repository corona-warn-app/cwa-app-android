package de.rki.coronawarnapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExternalActionException
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Helper object for external actions
 *
 */
object ExternalActionHelper {
    /**
     * Opens the share default overlay to provide the Corona-Warn-App installation link
     * @param text
     * @param title
     */
    fun Fragment.shareText(text: String, title: String?) {
        try {
            startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    },
                    title
                )
            )
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
     * @param phoneNumber
     */
    fun Fragment.callPhone(phoneNumber: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            )
        } catch (exception: Exception) {
            // catch generic exception on call
            // possibly due to bad number format
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    fun Fragment.openUrl(@StringRes urlRes: Int) = openUrl(getString(urlRes))
    fun Context.openUrl(@StringRes urlRes: Int) = openUrl(getString(urlRes))

    /**
     * Opens a given url in the client default browser
     * @param url
     */
    fun Fragment.openUrl(url: String) = requireContext().openUrl(url)

    /**
     * Opens a given url in the client default browser
     * @param url
     */
    fun Context.openUrl(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
     * Navigate the user to the os notification settings.
     */
    fun Context.openAppNotificationSettings() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    else -> {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", packageName)
                        putExtra("app_uid", applicationInfo.uid)
                    }
                }
            }
            startActivity(intent)
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
     */
    fun Fragment.openDeviceSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }

    /**
     * Opens Google play on CWA page
     */
    fun Context.openGooglePlay() {
        try {
            val uriStringInPlayStore = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uriStringInPlayStore)
                setPackage("com.android.vending")
            }.also {
                startActivity(it)
            }
        } catch (e: Exception) {
            ExternalActionException(e).report(ExceptionCategory.UI)
        }
    }

    /**
     * Open App's device details settings such as permissions
     */
    fun Context.openAppDetailsSettings() {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not open device settings")
            Toast.makeText(this, R.string.errors_generic_headline, Toast.LENGTH_LONG).show()
        }
    }
}
