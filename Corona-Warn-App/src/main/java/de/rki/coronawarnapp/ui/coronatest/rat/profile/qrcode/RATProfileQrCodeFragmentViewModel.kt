package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.coronatest.antigen.profile.VCard
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RATProfileQrCodeFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings,
    private val qrCodeGenerator: QrCodeGenerator,
    private val vCard: VCard,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    val profile: LiveData<PersonProfile> = ratProfileSettings.profile.flow
        .map { profile ->
            PersonProfile(
                profile,
                profile.qrCode()
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

    private suspend fun RATProfile?.qrCode(): Bitmap? =
        try {
            if (this != null) {
                qrCodeGenerator.createQrCode(vCard.create(this))
            } else {
                Timber.d("No Profile available")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate profile Qr Code")
            null
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileQrCodeFragmentViewModel>
}

data class PersonProfile(
    val profile: RATProfile?,
    val bitmap: Bitmap?
)
