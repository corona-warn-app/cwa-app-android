package de.rki.coronawarnapp.ui.coronatest.rat.profile.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RATProfileOnboardingFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings,
) : CWAViewModel() {

    fun onNext() {
        ratProfileSettings.onboarded.update { true }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileOnboardingFragmentViewModel>
}
