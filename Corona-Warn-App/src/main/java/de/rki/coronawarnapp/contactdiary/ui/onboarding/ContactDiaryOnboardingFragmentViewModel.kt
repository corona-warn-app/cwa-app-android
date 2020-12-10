package de.rki.coronawarnapp.contactdiary.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.ui.onboarding.OnboardingNavigationEvents
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory


class ContactDiaryOnboardingFragmentViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
    }


    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOnboardingFragmentViewModel>
}
