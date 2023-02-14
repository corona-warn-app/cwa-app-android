package de.rki.coronawarnapp.contactdiary.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class ContactDiaryOnboardingFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val settings: ContactDiaryUiSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen: SingleLiveEvent<ContactDiaryOnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.postValue(ContactDiaryOnboardingNavigationEvents.NavigateToOverviewFragment)
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOnboardingNavigationEvents.NavigateToMainActivity)
    }

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(ContactDiaryOnboardingNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onboardingComplete() = launch {
        settings.updateOnboardingStatus(onboardingStatus = ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12)
    }
}
