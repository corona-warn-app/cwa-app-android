package de.rki.coronawarnapp.ui.settings.backgroundpriority

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SettingsBackgroundPriorityFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    backgroundModeStatus: BackgroundModeStatus
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val backgroundPriorityState: LiveData<BackgroundPriorityState> =
        backgroundModeStatus.isIgnoringBatteryOptimizations
            .map {
                BackgroundPriorityState(isBackgroundPriorityEnabled = it)
            }
            .asLiveData(dispatcherProvider.Default)
}
