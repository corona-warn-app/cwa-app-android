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
import timber.log.Timber

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings
) : CWAViewModel() {

    private val profileData = MutableLiveData<RATProfile?>()
    val profile: LiveData<RATProfile?> = profileData
    val events = SingleLiveEvent<Navigation>()

    init {
        profileData.value = null
    }

    fun createProfile() {
        Timber.d("Profile=%s", profileData.value)
        if (profileData.value?.isValid == true) {
            ratProfileSettings.profile.update { profileData.value }
            Timber.d("Profile created")
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

    fun streetChanged(street: String) {
        profileData.value = profileData.value?.copy(street = street)
            ?: RATProfile(street = street)
    }

    fun zipCodeChanged(zipCode: String) {
        profileData.value = profileData.value?.copy(zipCode = zipCode)
            ?: RATProfile(zipCode = zipCode)
    }

    fun cityChanged(city: String) {
        profileData.value = profileData.value?.copy(city = city)
            ?: RATProfile(city = city)
    }

    fun phoneChanged(phone: String) {
        profileData.value = profileData.value?.copy(phone = phone)
            ?: RATProfile(phone = phone)
    }

    fun emailChanged(email: String) {
        profileData.value = profileData.value?.copy(email = email)
            ?: RATProfile(email = email)
    }

    fun navigateBack() {
        events.value = Navigation.Back
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileCreateFragmentViewModel>
}
