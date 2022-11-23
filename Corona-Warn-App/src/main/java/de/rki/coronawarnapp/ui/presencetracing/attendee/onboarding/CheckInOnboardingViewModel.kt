package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CheckInOnboardingViewModel @AssistedInject constructor(
    private val settings: TraceLocationSettings
) : CWAViewModel() {
    val events = SingleLiveEvent<CheckInOnboardingNavigation>()

    val isOnboardingComplete = settings.onboardingStatus.asLiveData2()

    fun onAcknowledged() = launch {
        settings.updateOnboardingStatus(TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0)
        events.postValue(CheckInOnboardingNavigation.AcknowledgedNavigation)
    }

    fun onPrivacy() {
        events.value = CheckInOnboardingNavigation.DataProtectionNavigation
    }

    fun onBackButtonPress() {
        events.value = CheckInOnboardingNavigation.AcknowledgedNavigation
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInOnboardingViewModel>
}
