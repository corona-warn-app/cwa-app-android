package de.rki.coronawarnapp.update

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

class InAppUpdateHandler(
    private val activity: Activity,
    private val onFailure: () -> Unit
) : LifecycleObserver {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    fun startImmediateUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            require(appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                "No Update Available on Play Store!"
            }

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UNKNOWN) {
                onFailure()
                return@addOnSuccessListener
            }

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    REQUEST_CODE
                )
            } else {
                onFailure()
            }
        }

        appUpdateManager.appUpdateInfo.addOnFailureListener {
            Timber.e(it, "Getting immediate update failed")
        }
    }

    /**
     * Check if there's an stale download, resume it
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun checkStaleDownload() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    REQUEST_CODE
                )
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            onFailure()
        }
    }

    private companion object {
        private const val REQUEST_CODE = 2293
    }
}
