package de.rki.coronawarnapp.profile.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.model.ProfileId
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ProfileCreateFragmentViewModel @AssistedInject constructor(
    private val profileRepository: ProfileRepository,
    @Assisted private val format: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
    @Assisted private val profileId: ProfileId?,
) : CWAViewModel() {

    private val profileData = MutableLiveData(Profile())
    internal val profile: LiveData<Profile> = profileData
    internal val events = SingleLiveEvent<CreateProfileNavigation>()
    internal val savedProfile = profileRepository.profilesFlow
        .map { profiles ->
            profiles.find { it.id == profileId }
        }.asLiveData()

    fun saveProfile() {
        val profileData = profileData.value
        Timber.d("Profile=%s", profileData)
        if (profileData?.isValid == true) {
            launch {
                Timber.d("Profile id is $profileId")
                val id = profileRepository.upsertProfile(
                    profileData.copy(id = profileId)
                )
                Timber.d("Profile created with id $id")
                events.postValue(CreateProfileNavigation.ProfileScreen(id))
            }
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
        events.value = CreateProfileNavigation.Back
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ProfileCreateFragmentViewModel> {
        fun create(
            formatter: DateTimeFormatter,
            profileId: ProfileId?
        ): ProfileCreateFragmentViewModel
    }
}
