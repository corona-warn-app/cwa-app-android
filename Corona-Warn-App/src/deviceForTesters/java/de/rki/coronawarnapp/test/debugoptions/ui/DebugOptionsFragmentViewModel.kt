package de.rki.coronawarnapp.test.debugoptions.ui

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.test.debugoptions.ui.EnvironmentState.Companion.toEnvironmentState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow

class DebugOptionsFragmentViewModel @AssistedInject constructor(
    private val envSetup: EnvironmentSetup,
    dispatcherProvider: DispatcherProvider,
    private val dscRepository: DscRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val environmentStateFlow = MutableStateFlow(envSetup.toEnvironmentState())
    val environmentState = environmentStateFlow.asLiveData(context = dispatcherProvider.Default)
    val environmentStateChange = SingleLiveEvent<EnvironmentState>()

    fun clearLaunchEnvironment() {
        envSetup.launchEnvironment = null
        envSetup.toEnvironmentState()
            .let {
                environmentStateFlow.value = it
                environmentStateChange.postValue(it)
            }
        cleanCachedData()
    }

    fun selectEnvironmentType(type: String) {
        envSetup.currentEnvironment = type.toEnvironmentType()
        envSetup.toEnvironmentState().let {
            environmentStateFlow.value = it
            environmentStateChange.postValue(it)
        }
        cleanCachedData()
    }

    private fun cleanCachedData() {
        launch {
            dscRepository.reset()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugOptionsFragmentViewModel>
}
