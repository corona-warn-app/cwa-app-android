package de.rki.coronawarnapp.profile.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileOnboardingFragmentViewModel @Inject constructor(
    private val profileSettings: ProfileSettingsDataStore,
) : CWAViewModel() {

    fun onNext() {
        profileSettings.setOnboarded()
    }
}
