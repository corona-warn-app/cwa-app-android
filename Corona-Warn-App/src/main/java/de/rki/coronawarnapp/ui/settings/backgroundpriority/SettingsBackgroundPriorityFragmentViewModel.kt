package de.rki.coronawarnapp.ui.settings.backgroundpriority

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class SettingsBackgroundPriorityFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    backgroundModeStatus: BackgroundModeStatus
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val backgroundPriorityState: LiveData<BackgroundPriorityState> =
        backgroundModeStatus.isIgnoringBatteryOptimizations
            .map {
                BackgroundPriorityState(isBackgroundPriorityEnabled = it)
            }
            .asLiveData(dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SettingsBackgroundPriorityFragmentViewModel>
}
