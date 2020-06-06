package de.rki.coronawarnapp.update

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.ui.LauncherActivity
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UpdateChecker(private val activity: LauncherActivity) {

    private val REQUEST_CODE = 100

    companion object {
        val TAG: String? = UpdateChecker::class.simpleName
    }


    /**
     *
     *  App start
     *      Check server fo min version
     *          if version is above min -> start app
     *          else
     *              Inform user that he needs to update with a dialog (dont close the dialog on button click)
     *                  User can only click on Update
     *                      google startUpdateFlow
     *                      google dialog -> do you want to update
     *                          yes -> update in fullscreen -> nav to app
     *                          no -> above dialog is still open to inform the user that he has to update & can click on update
     *                          fail -> retry counter?
     */
    private suspend fun checkForUpdate() {


        var updateNeededFromServer = false
        try {
            updateNeededFromServer = checkIfUpdatesNeededFromServer()
        }
        // TODO replace with signature exception
        catch (exception: Exception) {
            updateNeededFromServer = true
        }


        val baseContext = activity.baseContext

        var appUpdateManager: AppUpdateManager

        if (BuildConfig.DEBUG) {
            appUpdateManager = FakeAppUpdateManager(baseContext)

            appUpdateManager.setUpdateAvailable(1)
            //appUpdateManager.setUpdateNotAvailable()
        } else {
            appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        }

        var updateAvailableFromGooglePlay: Boolean
        try {
            val appUpdateInfoTask = checkForGooglePlayUpdate(appUpdateManager)

            val availability = appUpdateInfoTask.updateAvailability()
            val immediateUpdateAllowed =
                appUpdateInfoTask.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            updateAvailableFromGooglePlay =
                availability == UpdateAvailability.UPDATE_AVAILABLE && immediateUpdateAllowed
        } catch (exception: Exception) {
             updateAvailableFromGooglePlay = false
        }




        Log.i(TAG, "addOnSuccessListener")






        if (updateNeededFromServer) {
            showUpdateAvailableDialog()

        } else {
            activity.navigateToActivities()
        }

    }


    private fun showUpdateAvailableDialog() {

        AlertDialog.Builder(activity)
            .setTitle("Update verfügbar ")
            .setMessage("Update muss sein. sonst geht nix")
            .setPositiveButton("update") { _, _ ->

            }
            .create().show()


    }

    private suspend fun checkIfUpdatesNeededFromServer(): Boolean {

        val applicationConfigurationFromServer =
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()


        val latestVersionFromServer =
            applicationConfigurationFromServer.appVersion.android.latest
        val minVersionFromServer = applicationConfigurationFromServer.appVersion.android.min

        val latestVersionFromServerString =
            constructSemanticVersionString(latestVersionFromServer)
        val minVersionFromServerString =
            constructSemanticVersionString(minVersionFromServer)

        Log.i(
            TAG,
            "latestVersionStringFromServer:" + constructSemanticVersionString(
                latestVersionFromServer
            )
        )
        Log.i(
            TAG,
            "minVersionStringFromServer:" + constructSemanticVersionString(
                minVersionFromServer
            )
        )
        Log.i(TAG, "Current app version:" + BuildConfig.VERSION_NAME)

        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            BuildConfig.VERSION_NAME,
            minVersionFromServerString
        )
        Log.i(TAG, "needs update?:" + needsImmediateUpdate)
        return needsImmediateUpdate
    }

    fun doUpdate() {

        val baseContext = activity.baseContext

        var appUpdateManager: AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)

        if (BuildConfig.DEBUG) {
            appUpdateManager = FakeAppUpdateManager(baseContext)

            appUpdateManager.setUpdateAvailable(1)
            //appUpdateManager.setUpdateNotAvailable()
        } else {
            appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        }
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener {
            Log.i(TAG, "addOnSuccessListener")

            val updateAvailabilityOnPlayStore = appUpdateInfoTask.result.updateAvailability()
            val immediateUpdateAllowed =
                appUpdateInfoTask.result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            Log.i(TAG, "Availability:" + appUpdateInfoTask.result.updateAvailability())

            activity.lifecycleScope.launch {


                var updateAvailableOnGooglePlay =
                    updateAvailabilityOnPlayStore == UpdateAvailability.UPDATE_AVAILABLE && immediateUpdateAllowed

                if (updateAvailableOnGooglePlay) {
                    val startUpdateFlowTask = appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfoTask.result,
                        AppUpdateType.IMMEDIATE, activity, REQUEST_CODE
                    )

                } else {
                    Log.i(TAG, "navigating in else")
                    activity.navigateToActivities()
                }

                if (BuildConfig.DEBUG) {
                    val fakeAppUpdate = appUpdateManager as FakeAppUpdateManager
                    if (fakeAppUpdate.isImmediateFlowVisible) {
                        fakeAppUpdate.userAcceptsUpdate()
                        fakeAppUpdate.downloadStarts()
                        fakeAppUpdate.downloadCompletes()
                        launchRestartDialog(appUpdateManager)
                    }
                }
            }
        }

        appUpdateInfoTask.addOnFailureListener {
            Log.i(TAG, "addOnFailureListener")
            activity.navigateToActivities()
        }

    }

    private fun constructSemanticVersionString(
        semanticVersion: ApplicationConfigurationOuterClass.SemanticVersion
    ): String {
        return semanticVersion.major.toString() + "." +
                semanticVersion.minor.toString() + "." +
                semanticVersion.patch.toString()
    }

    private fun launchRestartDialog(manager: AppUpdateManager) {
        AlertDialog.Builder(activity)
            .setTitle("app update ")
            .setMessage("update successful")
            .setPositiveButton("restart") { _, _ ->
                manager.completeUpdate()
            }
            .create().show()

    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (REQUEST_CODE == requestCode) {

            when (resultCode) {
                RESULT_OK -> {
                    //app was successfully updated
                }
                RESULT_CANCELED -> {

                }
                RESULT_IN_APP_UPDATE_FAILED -> {

                }

            }
        }


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
