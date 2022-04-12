package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.VCard
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RATProfileQrCodeFragmentViewModel @AssistedInject constructor(
    private val profileRepository: ProfileRepository,
    private val vCard: VCard,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    // TO DO get id as nav arg
    private val id = 1

    private var qrCodeString: String? = null
    val personProfile: LiveData<PersonProfile> = profileRepository.profilesFlow
        .map { profiles ->
            val profile = profiles.find { it.id == id }
            PersonProfile(
                profile,
                profile?.let { vCard.create(it).also { qrCode -> qrCodeString = qrCode } }
            )
        }.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<ProfileQrCodeNavigation>()

    fun deleteProfile() {
        Timber.d("deleteProfile")
        personProfile.value?.profile?.id?.let {
            profileRepository.deleteProfile(it)
        }
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onClose() {
        Timber.d("onClose")
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onNext() {
        Timber.d("onNext")
        events.postValue(ProfileQrCodeNavigation.OpenScanner)
    }

    fun openFullScreen() = qrCodeString?.let {
        events.postValue(
            ProfileQrCodeNavigation.FullQrCode(CoilQrCode(it))
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileQrCodeFragmentViewModel>
}

data class PersonProfile(
    val profile: Profile?,
    val qrCode: String?
)
