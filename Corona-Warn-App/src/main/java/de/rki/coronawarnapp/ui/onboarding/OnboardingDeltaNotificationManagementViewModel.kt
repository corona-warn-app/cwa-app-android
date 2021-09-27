package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingDeltaNotificationManagementViewModel @AssistedInject constructor(
    private val settings: CWASettings,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val completedOnboardingEvent = SingleLiveEvent<Unit>()

    fun onProceed() {
        settings.lastOnboardingVersionCode.update { BuildConfigWrap.VERSION_CODE }
        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingDeltaNotificationManagementViewModel>
}

