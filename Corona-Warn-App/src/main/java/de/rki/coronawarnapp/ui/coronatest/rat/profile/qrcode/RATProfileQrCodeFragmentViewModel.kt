package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.VCard
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.model.ProfileId
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RATProfileQrCodeFragmentViewModel @AssistedInject constructor(
    private val profileRepository: ProfileRepository,
    private val vCard: VCard,
    @Assisted private val profileId: ProfileId,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {
    private var qrCodeString: String? = null
    val personProfile: LiveData<PersonProfile> = profileRepository.profilesFlow
        .map { profiles ->
            val profile = profiles.find { it.id == profileId } ?: Profile()
            PersonProfile(
                profile = profile,
                qrCode = vCard.create(profile)
            )
        }.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<ProfileQrCodeNavigation>()

    fun deleteProfile() = launch {
        Timber.d("deleteProfile")
        profileRepository.deleteProfile(profileId)
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onClose() {
        Timber.d("onClose")
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onNext() {
        Timber.d("onNext")
        val personName: String = personProfile.value?.profile?.let {
            "${it.firstName} ${it.lastName}".trim()
        } ?: ""
        events.postValue(
            ProfileQrCodeNavigation.OpenScanner(
                personName
            )
        )
    }

    fun openFullScreen() = qrCodeString?.let {
        events.postValue(
            ProfileQrCodeNavigation.FullQrCode(CoilQrCode(it))
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RATProfileQrCodeFragmentViewModel> {
        fun create(id: Int): RATProfileQrCodeFragmentViewModel
    }
}

data class PersonProfile(
    val profile: Profile,
    val qrCode: String?
)
