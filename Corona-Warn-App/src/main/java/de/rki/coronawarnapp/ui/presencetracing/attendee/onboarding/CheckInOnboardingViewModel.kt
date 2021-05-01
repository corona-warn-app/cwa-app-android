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

    fun onAcknowledged() {
        settings.onboardingStatus.update {
            TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0
        }
        events.value = CheckInOnboardingNavigation.AcknowledgedNavigation
    }

    fun onPrivacy() {
        events.value = CheckInOnboardingNavigation.DataProtectionNavigation
    }

    fun onBackButtonPress() {
        events.value = CheckInOnboardingNavigation.AcknowledgedNavigation
    }

    val isOnboardingComplete = settings.onboardingStatus.value == TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInOnboardingViewModel>
}
