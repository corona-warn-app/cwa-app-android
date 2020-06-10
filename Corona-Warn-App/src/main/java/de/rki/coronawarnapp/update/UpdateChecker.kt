package de.rki.coronawarnapp.update

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.IntentSender.SendIntentException
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.ui.LauncherActivity
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UpdateChecker(private val activity: LauncherActivity) {

    companion object {
        val TAG: String? = UpdateChecker::class.simpleName
        private const val REQUEST_CODE = 100
    }

    suspend fun checkForUpdate() {

        // check if an update is needed based on server config
        val updateNeededFromServer: Boolean = try {
            checkIfUpdatesNeededFromServer()
        } catch (exception: CwaSecurityException) {
            Timber.e("CwaSecurityException caught:$exception.localizedMessage")
            true
        } catch (exception: Exception) {
            Timber.e("Exception caught:$exception.localizedMessage")
            false
        }

        // get AppUpdateManager
        val baseContext = activity.baseContext
        val appUpdateManager = AppUpdateManagerFactory.create(baseContext)

        var appUpdateInfo: AppUpdateInfo? = null

        val updateAvailableFromGooglePlay = try {
            appUpdateInfo = checkForGooglePlayUpdate(appUpdateManager)

            val availability = appUpdateInfo.updateAvailability()
            val immediateUpdateAllowed =
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            availability == UpdateAvailability.UPDATE_AVAILABLE && immediateUpdateAllowed
        } catch (exception: Exception) {
            false
        }

        if (updateNeededFromServer && updateAvailableFromGooglePlay && appUpdateInfo != null) {
            Timber.i("show update dialog")
            showUpdateAvailableDialog(appUpdateManager, appUpdateInfo)
        } else {
            activity.navigateToActivities()
        }
    }

    private fun showUpdateAvailableDialog(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo
    ) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.update_dialog_title))
            .setMessage(activity.getString(R.string.update_dialog_message))
            .setPositiveButton(activity.getString(R.string.update_dialog_button)) { _, _ ->
                startGooglePlayUpdateFlow(appUpdateManager, appUpdateInfo)
            }
            .create().show()
    }

    private fun startGooglePlayUpdateFlow(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                activity,
                REQUEST_CODE
            )
        } catch (exception: SendIntentException) {
            Timber.i(exception.toString())
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (REQUEST_CODE == requestCode) {

            // TODO react to these
            when (resultCode) {
                RESULT_OK -> {
                    Timber.i("startFlowResult RESULT_OK")
                    activity.navigateToActivities()
                }
                RESULT_CANCELED -> {
                    Timber.i("startFlowResult RESULT_CANCELED")
                }
                RESULT_IN_APP_UPDATE_FAILED -> {
                    Timber.i("startFlowResult RESULT_IN_APP_UPDATE_FAILED")
                    val toast = Toast.makeText(activity, "In app update failed", Toast.LENGTH_LONG)
                    toast.show()
                    activity.navigateToActivities()
                }
            }
        }
    }

    private suspend fun checkIfUpdatesNeededFromServer(): Boolean {

        val applicationConfigurationFromServer =
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()

        val minVersionFromServer = applicationConfigurationFromServer.appVersion.android.min
        val minVersionFromServerString =
            constructSemanticVersionString(minVersionFromServer)
        Timber.i("minVersionStringFromServer:${constructSemanticVersionString(minVersionFromServer)}")
        Timber.i("Current app version:${BuildConfig.VERSION_NAME}")

        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            BuildConfig.VERSION_NAME,
            minVersionFromServerString
        )
        Timber.i("needs update:$needsImmediateUpdate")
        return true
    }

    private fun constructSemanticVersionString(
        semanticVersion: ApplicationConfigurationOuterClass.SemanticVersion
    ): String {
        return semanticVersion.major.toString() + "." +
                semanticVersion.minor.toString() + "." +
                semanticVersion.patch.toString()
    }

    private suspend fun checkForGooglePlayUpdate(appUpdateManager: AppUpdateManager) =
        suspendCoroutine<AppUpdateInfo> { cont ->
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
}
