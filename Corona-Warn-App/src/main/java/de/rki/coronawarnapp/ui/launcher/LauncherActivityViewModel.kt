package de.rki.coronawarnapp.ui.launcher

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.rootdetection.RootDetectionCheck
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class LauncherActivityViewModel @AssistedInject constructor(
    private val updateChecker: UpdateChecker,
    dispatcherProvider: DispatcherProvider,
    private val cwaSettings: CWASettings,
    private val onboardingSettings: OnboardingSettings,
    private val rootDetectionCheck: RootDetectionCheck
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<LauncherEvent>()

    init {
        Timber.tag(TAG).d("init()")
        checkForRoot()
    }

    private fun checkForRoot() = launch {
        Timber.tag(TAG).d("checkForRoot()")
        when (rootDetectionCheck.isRooted()) {
            true -> events.postValue(LauncherEvent.ShowRootedDialog)
            false -> checkForUpdate()
        }
    }

    private fun checkForUpdate() = launch {
        Timber.tag(TAG).d("checkForUpdate()")
        val updateResult = updateChecker.checkForUpdate()
        when {
            updateResult.isUpdateNeeded -> LauncherEvent.ShowUpdateDialog(updateResult.updateIntent?.invoke()!!)
            isJustInstalledOrUpdated() -> LauncherEvent.GoToOnboarding
            else -> LauncherEvent.GoToMainActivity
        }.let { events.postValue(it) }
    }

    fun onRootedDialogDismiss() {
        Timber.tag(TAG).d("onRootedDialogDismiss()")
        checkForUpdate()
    }

    private fun isJustInstalledOrUpdated() =
        !onboardingSettings.isOnboarded || !cwaSettings.wasInteroperabilityShownAtLeastOnce ||
            cwaSettings.lastChangelogVersion.value < BuildConfigWrap.VERSION_CODE

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<LauncherActivityViewModel>

    companion object {
        private val TAG = tag<LauncherActivityViewModel>()
    }
}
