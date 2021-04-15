package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings
) : CWAViewModel() {

    private val profileData = MutableLiveData<RATProfile?>()
    val profile: LiveData<RATProfile?> = profileData

    init {
        profileData.value = null
    }

    fun saveProfile() {
        ratProfileSettings.profile.update { profileData.value }
    }

    fun firstNameChanged(firstName: String) {
        profileData.value = profileData.value?.copy(firstName = firstName)
            ?: RATProfile(firstName = firstName)
    }

    fun lastNameChanged(lastName: String) {
        profileData.value = profileData.value?.copy(lastName = lastName)
            ?: RATProfile(lastName = lastName)
    }

    fun birthDateChanged(birthDate: String) {
        profileData.value = profileData.value?.copy(birthDate = birthDate)
            ?: RATProfile(birthDate = birthDate)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileCreateFragmentViewModel>
}
