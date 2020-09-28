package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestForApiFragmentViewModel @AssistedInject constructor(
    private val environmentSetup: EnvironmentSetup
) : CWAViewModel() {

    val last3HourToggleEvent = SingleLiveEvent<Boolean>()
    val environmentSetupToggleEvent = SingleLiveEvent<Boolean>()

    fun setLast3HoursMode(isLast3HoursModeEnabled: Boolean) {
        LocalData.last3HoursMode(isLast3HoursModeEnabled)
        last3HourToggleEvent.postValue(isLast3HoursModeEnabled)
    }

    fun toggleEnvironment(isTestCountyEnabled: Boolean) {
        if (isTestCountyEnabled) {
            environmentSetup.currentEnvironment = EnvironmentSetup.Type.WRU_XA
        } else {
            environmentSetup.currentEnvironment = environmentSetup.defaultEnvironment
        }
        environmentSetupToggleEvent.postValue(isTestCountyEnabled)
    }

    fun isTestCountyCurrentEnvironment(): Boolean {
        if (environmentSetup.currentEnvironment != EnvironmentSetup.Type.WRU_XA)
            return false
        return true
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestForApiFragmentViewModel>
}
