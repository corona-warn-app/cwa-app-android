package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingAnalyticsViewModel @AssistedInject constructor() : CWAViewModel() {

    val completedOnboardingEvent = SingleLiveEvent<Unit>()

    fun onNextButtonClick() {
        // TODO: Some logic here
        completedOnboardingEvent.postValue(Unit)
    }

    fun onDisableClick() {
        // TODO: Some logic here
        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingAnalyticsViewModel>
}
