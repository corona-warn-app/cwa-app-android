package de.rki.coronawarnapp.test.debugoptions.ui

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.eol.EolSetting
import de.rki.coronawarnapp.test.debugoptions.ui.EnvironmentState.Companion.toEnvironmentState
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DebugOptionsFragmentViewModel @AssistedInject constructor(
    private val envSetup: EnvironmentSetup,
    private val eolSetting: EolSetting,
    private val eol: AppEol,
    dispatcherProvider: DispatcherProvider,
    private val environmentSunset: EnvironmentSunset,
    @AppScope private val appScope: CoroutineScope,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val environmentStateFlow = MutableStateFlow(envSetup.toEnvironmentState())
    val environmentState = environmentStateFlow.asLiveData(context = dispatcherProvider.Default)
    val environmentStateChange = SingleLiveEvent<EnvironmentState>()
    val isLoggerAllowed = eolSetting.isLoggerAllowed.asLiveData2()

    fun clearLaunchEnvironment() {
        envSetup.launchEnvironment = null
        envSetup.toEnvironmentState()
            .let {
                environmentStateFlow.value = it
                environmentStateChange.postValue(it)
            }
        cleanCachedDataIfNotEol()
    }

    fun selectEnvironmentType(type: String) {
        envSetup.currentEnvironment = type.toEnvironmentType()
        envSetup.toEnvironmentState().let {
            environmentStateFlow.value = it
        }
        cleanCachedDataIfNotEol()
    }

    fun setAllowedFlag(flag: Boolean) = launch {
        eolSetting.setLoggerAllowed(flag)
    }

    private fun cleanCachedDataIfNotEol() = appScope.launch {
        if (!eol.isEol.first()) {
            environmentSunset.reset()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugOptionsFragmentViewModel>
}
