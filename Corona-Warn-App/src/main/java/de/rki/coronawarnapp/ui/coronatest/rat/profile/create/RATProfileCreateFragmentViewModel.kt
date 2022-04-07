package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import timber.log.Timber

class RATProfileCreateFragmentViewModel @AssistedInject constructor(
    private val profileRepository: ProfileRepository,
    @Assisted private val format: DateTimeFormatter = DateTimeFormat.mediumDate()
) : CWAViewModel() {

    // TO DO get id as nav arg
    private val id: String? = "1"

    // TO DO check logic
    private val profileData = MutableLiveData(Profile())
    val profile: LiveData<Profile> = profileData
    val events = SingleLiveEvent<CreateRATProfileNavigation>()

    val savedProfile = profileRepository.profilesFlow
        .map { profiles ->
            profiles.find { it.id == id }
        }.asLiveData()

    fun createProfile() {
        val profileData = profileData.value
        Timber.d("Profile=%s", profileData)
        if (profileData?.isValid == true) {
            profileRepository.upsertProfile(profileData.copy(id = id))
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

    fun birthDateChanged(birthDate: String?) {
        profileData.apply {
            value = value?.copy(birthDate = parseDate(birthDate))
        }
    }

    private fun parseDate(birthDate: String?): LocalDate? {
        return birthDate?.let {
            try {
                LocalDate.parse(birthDate, format)
            } catch (e: Exception) {
                Timber.d(e, "Malformed date")
                null
            }
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
    interface Factory : CWAViewModelFactory<RATProfileCreateFragmentViewModel> {
        fun create(formatter: DateTimeFormatter): RATProfileCreateFragmentViewModel
    }
}
