package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.LocalDate
import timber.log.Timber

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings
) : CWAViewModel() {

    private val profileData = MutableLiveData(RATProfileData())
    val profile: LiveData<RATProfileData?> = profileData
    val events = SingleLiveEvent<CreateRATProfileNavigation>()

    fun createProfile() {
        val ratProfileData = profileData.value
        Timber.d("Profile=%s", ratProfileData)
        if (ratProfileData?.isValid == true) {
            ratProfileSettings.profile.update { ratProfileData.toRATProfile() }
            Timber.d("Profile created")
            events.value = CreateRATProfileNavigation.ProfileScreen
        }
    }

    fun firstNameChanged(firstName: String) {
        profileData.apply {
            value = value?.copy(firstName = firstName)
        }
    }

    fun lastNameChanged(lastName: String) {
        profileData.apply {
            value = value?.copy(lastName = lastName)
        }
    }

    fun birthDateChanged(birthDate: LocalDate?) {
        profileData.apply {
            value = value?.copy(birthDate = birthDate)
        }
    }

    fun streetChanged(street: String) {
        profileData.apply {
            value = value?.copy(street = street)
        }
    }

    fun zipCodeChanged(zipCode: String) {
        profileData.apply {
            value = value?.copy(zipCode = zipCode)
        }
    }

    fun cityChanged(city: String) {
        profileData.apply {
            value = value?.copy(city = city)
        }
    }

    fun phoneChanged(phone: String) {
        profileData.apply {
            value = value?.copy(phone = phone)
        }
    }

    fun emailChanged(email: String) {
        profileData.apply {
            value = value?.copy(email = email)
        }
    }

    fun navigateBack() {
        events.value = CreateRATProfileNavigation.Back
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileCreateFragmentViewModel>
}
