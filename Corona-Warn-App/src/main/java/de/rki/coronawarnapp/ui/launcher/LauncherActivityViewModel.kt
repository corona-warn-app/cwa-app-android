package de.rki.coronawarnapp.ui.launcher

import android.app.Activity.RESULT_OK
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.rootdetection.core.RootDetectionCheck
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.update.getUpdateInfo
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class LauncherActivityViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val updateChecker: UpdateChecker,
    private val cwaSettings: CWASettings,
    private val onboardingSettings: OnboardingSettings,
    private val rootDetectionCheck: RootDetectionCheck,
    private val appUpdateManager: AppUpdateManager
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<LauncherEvent>()

    init {
        Timber.tag(TAG).d("init()")
        checkForRoot()
    }

    fun onRootedDialogDismiss() {
        Timber.tag(TAG).d("onRootedDialogDismiss()")
        checkForUpdate()
    }

    fun onResume() = launch {
        Timber.tag(TAG).d("onResume()")
        val appUpdateInfo = appUpdateManager.getUpdateInfo()
        Timber.tag(TAG).d("onResume - appUpdateInfo=%s", appUpdateInfo?.updateAvailability())
        if (appUpdateInfo?.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            events.postValue(forceUpdateEvent(appUpdateInfo))
        }
    }

    fun onResult(requestCode: Int, resultCode: Int) {
        Timber.tag(TAG).d("onResult(requestCode=$requestCode, resultCode=$resultCode)")
        if (requestCode == UPDATE_CODE && resultCode != RESULT_OK) {
            Timber.tag(TAG).d("Update flow failed! Result code: $resultCode")
            // If the update is cancelled or fails, request to start the update again.
            events.postValue(LauncherEvent.ShowUpdateDialog)
        }
    }

    fun requestUpdate() = launch {
        val appUpdateInfo = appUpdateManager.getUpdateInfo()
        Timber.tag(TAG).d("checkForUpdate - appUpdateInfo=%s", appUpdateInfo)
        if (appUpdateInfo?.updateAvailability() == UPDATE_AVAILABLE) {
            events.postValue(forceUpdateEvent(appUpdateInfo))
        }
    }

    private fun forceUpdateEvent(appUpdateInfo: AppUpdateInfo): LauncherEvent {
        Timber.tag(TAG).d("forceUpdateEvent(appUpdateInfo=%s)", appUpdateInfo)
        return LauncherEvent.ForceUpdate { activity ->
            try {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, activity, UPDATE_CODE)
            } catch (e: Exception) {
                Timber.tag(TAG).d("startUpdateFlowForResult failed for appUpdateInfo=$appUpdateInfo")
                Timber.tag(TAG).d("startUpdateFlowForResult - Ask user to try again")
                events.postValue(LauncherEvent.ShowUpdateDialog)
            }
        }
    }

    private fun checkForRoot() = launch {
        Timber.tag(TAG).d("checkForRoot()")
        when (rootDetectionCheck.shouldShowRootInfo()) {
            true -> events.postValue(LauncherEvent.ShowRootedDialog)
            false -> checkForUpdate()
        }
    }

    private fun checkForUpdate() = launch {
        Timber.tag(TAG).d("checkForUpdate()")
        val updateResult = updateChecker.checkForUpdate()
        val appUpdateInfo = appUpdateManager.getUpdateInfo()
        Timber.tag(TAG).d("checkForUpdate - appUpdateInfo=%s, updateResult=%s", appUpdateInfo, updateResult)
        when {
            // Trigger update process ONLY when AppConfig and InAppUpdate are both indicating there is an update
            updateResult.isUpdateNeeded && appUpdateInfo?.updateAvailability() == UPDATE_AVAILABLE ->
                LauncherEvent.ShowUpdateDialog

            isJustInstalledOrUpdated() -> LauncherEvent.GoToOnboarding
            else -> LauncherEvent.GoToMainActivity
        }.let { events.postValue(it) }
    }

    private fun isJustInstalledOrUpdated() =
        !onboardingSettings.isOnboarded || !cwaSettings.wasInteroperabilityShownAtLeastOnce ||
            cwaSettings.lastChangelogVersion.value < BuildConfigWrap.VERSION_CODE

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<LauncherActivityViewModel>

    companion object {
        const val UPDATE_CODE = 90000
        private val TAG = tag<LauncherActivityViewModel>()
    }
}
