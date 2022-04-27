package de.rki.coronawarnapp.profile.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ProfileOnboardingFragmentViewModel @AssistedInject constructor(
    private val profileSettings: ProfileSettingsDataStore,
) : CWAViewModel() {

    fun onNext() {
        profileSettings.setOnboarded()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ProfileOnboardingFragmentViewModel>
}
