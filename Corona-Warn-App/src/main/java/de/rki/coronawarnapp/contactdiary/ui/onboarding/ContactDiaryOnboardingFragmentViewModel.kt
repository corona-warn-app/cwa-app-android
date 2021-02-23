package de.rki.coronawarnapp.contactdiary.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryOnboardingFragmentViewModel @AssistedInject constructor() : CWAViewModel() {
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

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOnboardingFragmentViewModel>
}
