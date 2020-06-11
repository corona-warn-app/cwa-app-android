package de.rki.coronawarnapp.update

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.ui.LauncherActivity
import timber.log.Timber

class UpdateChecker(private val activity: LauncherActivity) {

    companion object {
        val TAG: String? = UpdateChecker::class.simpleName

        const val STORE_PREFIX = "https://play.google.com/store/apps/details?id="
        const val COM_ANDROID_VENDING = "com.android.vending"
    }

    suspend fun checkForUpdate() {
        // check if an update is needed based on server config
        val updateNeededFromServer: Boolean = try {
            checkIfUpdatesNeededFromServer()
        } catch (exception: ApplicationConfigurationCorruptException) {
            Timber.e(
                "ApplicationConfigurationCorruptException caught:%s",
                exception.localizedMessage
            )
            true
        } catch (exception: Exception) {
            Timber.e("Exception caught:%s", exception.localizedMessage)
            false
        }

        if (updateNeededFromServer) {
            showUpdateNeededDialog()
        } else {
            activity.navigateToActivities()
        }
    }

    /**
     * Show dialog there an update is needed and links to the play store
     */
    private fun showUpdateNeededDialog() {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.update_dialog_title))
            .setMessage(activity.getString(R.string.update_dialog_message))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.update_dialog_button)) { _, _ ->

                val uriStringInPlayStore = STORE_PREFIX + BuildConfig.APPLICATION_ID
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(
                        uriStringInPlayStore
                    )
                    setPackage(COM_ANDROID_VENDING)
                }
                startActivity(activity, intent, null)
            }
            .create().show()
    }

    private suspend fun checkIfUpdatesNeededFromServer(): Boolean {
        val applicationConfigurationFromServer =
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()

        val minVersionFromServer = applicationConfigurationFromServer.appVersion.android.min
        val minVersionFromServerString =
            constructSemanticVersionString(minVersionFromServer)

        Timber.e(
            "minVersionStringFromServer:%s", constructSemanticVersionString(
                minVersionFromServer
            )
        )
        Timber.e("Current app version:%s", BuildConfig.VERSION_NAME)

        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            BuildConfig.VERSION_NAME,
            minVersionFromServerString
        )
        Timber.e("needs update:$needsImmediateUpdate")
        return needsImmediateUpdate
    }

    private fun constructSemanticVersionString(
        semanticVersion: ApplicationConfigurationOuterClass.SemanticVersion
    ): String {
        return semanticVersion.major.toString() + "." +
                semanticVersion.minor.toString() + "." +
                semanticVersion.patch.toString()
    }
}
