package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings
) : CWAViewModel() {

    private val profileData = MutableLiveData<RATProfile?>()
    val profile: LiveData<RATProfile?> = profileData
    val events = SingleLiveEvent<Navigation>()

    init {
        profileData.value = null
    }

    fun saveProfile() {
        if (profileData.value?.isValid == true) {
            ratProfileSettings.profile.update { profileData.value }
            events.value = Navigation.ProfileScreen
        }
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

    fun navigateBack() {
        events.value = Navigation.Back
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileCreateFragmentViewModel>
}
