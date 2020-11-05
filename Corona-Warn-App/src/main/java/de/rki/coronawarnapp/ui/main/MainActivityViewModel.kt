package de.rki.coronawarnapp.ui.main

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class MainActivityViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val environmentSetup: EnvironmentSetup
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val showEnvironmentHint = SingleLiveEvent<String>()

    init {
        if (CWADebug.isDeviceForTestersBuild) {
            launch {
                val current = environmentSetup.currentEnvironment
                if (current != EnvironmentSetup.Type.PRODUCTION) {
                    showEnvironmentHint.postValue(current.rawKey)
                }
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<MainActivityViewModel>
}
