package de.rki.coronawarnapp.ui.launcher

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class LauncherActivityViewModel @AssistedInject constructor(
    private val updateChecker: UpdateChecker,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<LauncherEvent>()

    init {
        launch {
            val updateResult = updateChecker.checkForUpdate()
            when {
                updateResult.isUpdateNeeded -> LauncherEvent.ShowUpdateDialog(updateResult.updateIntent?.invoke()!!)
                LocalData.isOnboarded() -> LauncherEvent.GoToMainActivity
                else -> LauncherEvent.GoToOnboarding
            }.let { events.postValue(it) }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<LauncherActivityViewModel>
}
