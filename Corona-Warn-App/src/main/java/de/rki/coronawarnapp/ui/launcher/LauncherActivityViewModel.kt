package de.rki.coronawarnapp.ui.launcher

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class LauncherActivityViewModel @AssistedInject constructor(
    private val updateChecker: UpdateChecker,
    dispatcherProvider: DispatcherProvider,
    private val cwaSettings: CWASettings,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<LauncherEvent>()

    init {
        launch {
            val updateResult = updateChecker.checkForUpdate()
            when {
                updateResult.isUpdateNeeded -> LauncherEvent.ShowUpdateDialog(updateResult.updateIntent?.invoke()!!)
                isJustInstalledOrUpdated() -> LauncherEvent.GoToOnboarding
                else -> LauncherEvent.GoToMainActivity
            }.let { events.postValue(it) }
        }
    }

    private fun isJustInstalledOrUpdated() =
        !LocalData.isOnboarded() || !LocalData.isInteroperabilityShownAtLeastOnce ||
            cwaSettings.lastChangelogVersion.value < BuildConfigWrap.VERSION_CODE

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<LauncherActivityViewModel>
}
