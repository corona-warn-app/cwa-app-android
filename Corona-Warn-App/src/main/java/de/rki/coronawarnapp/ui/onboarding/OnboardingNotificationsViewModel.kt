package de.rki.coronawarnapp.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingNotificationsViewModel @AssistedInject constructor() : CWAViewModel() {

    val completedOnboardingEvent = SingleLiveEvent<Unit>()

    fun onNextButtonClick() {
        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingNotificationsViewModel>
}
