package de.rki.coronawarnapp.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingNotificationsViewModel @Inject constructor(
    private val settings: CWASettings
) : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() = launch {
        settings.updateLastNotificationsOnboardingVersionCode(BuildConfigWrap.VERSION_CODE)
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingAnalytics)
    }
}
