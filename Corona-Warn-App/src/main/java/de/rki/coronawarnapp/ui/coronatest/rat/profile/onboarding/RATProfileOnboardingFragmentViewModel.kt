package de.rki.coronawarnapp.ui.coronatest.rat.profile.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RATProfileOnboardingFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettingsDataStore,
) : CWAViewModel() {

    fun onNext() {
        ratProfileSettings.setOnboarded()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileOnboardingFragmentViewModel>
}
