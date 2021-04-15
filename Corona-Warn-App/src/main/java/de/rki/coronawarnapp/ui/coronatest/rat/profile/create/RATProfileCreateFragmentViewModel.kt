package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings
) : CWAViewModel() {
    fun saveProfile() {

        ratProfileSettings.profile.update {
            RATProfile(
                firstName = "Max",
                lastName = "Mustermann",
                birthDate = "01.10.2000"
            )
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileCreateFragmentViewModel>
}
