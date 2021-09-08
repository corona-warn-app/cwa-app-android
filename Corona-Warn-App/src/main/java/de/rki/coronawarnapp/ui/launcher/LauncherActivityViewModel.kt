package de.rki.coronawarnapp.ui.launcher

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.update.getUpdateInfo
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class LauncherActivityViewModel @AssistedInject constructor(
    private val appUpdateManager: AppUpdateManager,
    dispatcherProvider: DispatcherProvider,
    private val cwaSettings: CWASettings,
    private val onboardingSettings: OnboardingSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<LauncherEvent>()

    init {
        launch {
            val appUpdateInfo = appUpdateManager.getUpdateInfo()
            Timber.tag(TAG).d("appUpdateInfo=%s", appUpdateInfo?.updateAvailability())
            when {
                appUpdateInfo?.updateAvailability() == UPDATE_AVAILABLE -> forceUpdateEvent(appUpdateInfo)
                isJustInstalledOrUpdated() -> LauncherEvent.GoToOnboarding
                else -> LauncherEvent.GoToMainActivity
            }.let { events.postValue(it) }
        }
    }

    fun onResume() = launch {
        val appUpdateInfo = appUpdateManager.getUpdateInfo()
        if (appUpdateInfo?.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            events.postValue(forceUpdateEvent(appUpdateInfo))
        }
    }

    fun onResult(requestCode: Int, resultCode: Int) {
        if (requestCode == UPDATE_CODE) {
            when (resultCode) {
                // The user has accepted the update. For immediate updates, this callback might not be called
                // because the update should already be finished by the time control is given back to your app.
                RESULT_OK -> {
                    // TODO
                }

                // The user has denied or canceled the update.
                RESULT_CANCELED -> {
                    // TODO
                }

                // Some other error prevented either the user from providing consent or the update from proceeding.
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    // TODO
                }
            }
        }
    }

    private fun forceUpdateEvent(appUpdateInfo: AppUpdateInfo) =
        LauncherEvent.ForceUpdate { activity ->
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, activity, UPDATE_CODE)
        }

    private fun isJustInstalledOrUpdated() =
        !onboardingSettings.isOnboarded || !cwaSettings.wasInteroperabilityShownAtLeastOnce ||
            cwaSettings.lastChangelogVersion.value < BuildConfigWrap.VERSION_CODE

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<LauncherActivityViewModel>

    companion object {
        private const val UPDATE_CODE = 90000
        private val TAG = tag<LauncherActivityViewModel>()
    }
}
