package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.coronatest.antigen.profile.VCard
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RATProfileQrCodeFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings,
    private val vCard: VCard,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    private var qrCodeString: String? = null
    val profile: LiveData<PersonProfile> = ratProfileSettings.profile.flow
        .map { profile ->
            PersonProfile(
                profile,
                profile?.let { vCard.create(it).also { qrCode -> qrCodeString = qrCode } }
            )
        }.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<ProfileQrCodeNavigation>()

    fun deleteProfile() {
        Timber.d("deleteProfile")
        ratProfileSettings.deleteProfile()
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onClose() {
        Timber.d("onClose")
        events.postValue(ProfileQrCodeNavigation.Back)
    }

    fun onNext() {
        Timber.d("onNext")
        events.postValue(ProfileQrCodeNavigation.SubmissionConsent)
    }

    fun openFullScreen() = qrCodeString?.let {
        events.postValue(
            ProfileQrCodeNavigation.FullQrCode(it)
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileQrCodeFragmentViewModel>
}

data class PersonProfile(
    val profile: RATProfile?,
    val qrCode: String?
)
