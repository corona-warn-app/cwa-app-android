package de.rki.coronawarnapp.test.debugoptions.ui

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.test.debugoptions.ui.EnvironmentState.Companion.toEnvironmentState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.smartLiveData
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DebugOptionsFragmentViewModel @AssistedInject constructor(
    private val envSetup: EnvironmentSetup,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val environmentState by smartLiveData {
        envSetup.toEnvironmentState()
    }
    val environmentChangeEvent = SingleLiveEvent<EnvironmentSetup.Type>()

    fun selectEnvironmentTytpe(type: String) {
        environmentState.update {
            envSetup.currentEnvironment = type.toEnvironmentType()
            environmentChangeEvent.postValue(envSetup.currentEnvironment)
            envSetup.toEnvironmentState()
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<DebugOptionsFragmentViewModel>
}
