package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class CheckInOnboardingViewModel @Inject constructor(
    private val settings: TraceLocationSettings
) : CWAViewModel() {
    val events = SingleLiveEvent<CheckInOnboardingNavigation>()

    fun onAcknowledged() = launch {
        settings.updateOnboardingStatus(TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0)
        events.postValue(CheckInOnboardingNavigation.AcknowledgedNavigation)
    }

    fun checkOnboarding() = launch {
        if (settings.onboardingStatus.first() == TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0) {
            events.postValue(CheckInOnboardingNavigation.SkipOnboardingInfo)
        }
    }

    fun onPrivacy() {
        events.value = CheckInOnboardingNavigation.DataProtectionNavigation
    }

    fun onBackButtonPress() {
        events.value = CheckInOnboardingNavigation.AcknowledgedNavigation
    }
}
