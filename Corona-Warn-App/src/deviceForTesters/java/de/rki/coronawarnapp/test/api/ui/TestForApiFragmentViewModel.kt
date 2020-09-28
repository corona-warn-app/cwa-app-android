package de.rki.coronawarnapp.test.api.ui

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestForApiFragmentViewModel @AssistedInject constructor(
    private val envSetup: EnvironmentSetup
) : CWAViewModel() {

    val last3HourToggleEvent = SingleLiveEvent<Boolean>()
    val environmentChangeEvent = SingleLiveEvent<EnvironmentSetup.Type>()

    fun setLast3HoursMode(isLast3HoursModeEnabled: Boolean) {
        LocalData.last3HoursMode(isLast3HoursModeEnabled)
        last3HourToggleEvent.postValue(isLast3HoursModeEnabled)
    }

    fun toggleEnvironment(isTestCountyEnabled: Boolean) {
        if (isTestCountyEnabled) {
            envSetup.currentEnvironment = envSetup.alternativeEnvironment
        } else {
            envSetup.currentEnvironment = envSetup.defaultEnvironment
        }
        environmentChangeEvent.postValue(envSetup.currentEnvironment)
    }

    fun isTestCountyCurrentEnvironment(): Boolean {
        return envSetup.currentEnvironment == envSetup.alternativeEnvironment
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestForApiFragmentViewModel>
}
