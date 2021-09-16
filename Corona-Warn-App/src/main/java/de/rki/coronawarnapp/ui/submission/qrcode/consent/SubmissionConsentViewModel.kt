package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.google.android.gms.common.api.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class SubmissionConsentViewModel @AssistedInject constructor(
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider,
    @Assisted private val qrCode: String,
    @Assisted private val allowReplacement: Boolean,
    private val tekHistoryProvider: TEKHistoryProvider,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val qrCodeValidator: CoronaTestQrCodeValidator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val qrCodeError = SingleLiveEvent<Exception>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    val countries = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun onConsentButtonClick() {
        launch {
            try {
                val preAuthorized = tekHistoryProvider.preAuthorizeExposureKeyHistory()
                // Proceed anyway, either user has already granted permission or it is older Api
                processQrCode(qrCode)
                Timber.i("Pre-authorized:$preAuthorized")
            } catch (exception: Exception) {
                if (exception is ApiException &&
                    exception.status.hasResolution()
                ) {
                    Timber.d(exception, "Pre-auth requires user resolution")
                    routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
                } else {
                    Timber.d(exception, "Pre-auth failed with unrecoverable exception")
                    processQrCode(qrCode)
                }
            }
        }
    }

    private fun processQrCode(qrCodeString: String) {
        launch {
            validateAndRegister(qrCodeString)
        }
    }

    private suspend fun validateAndRegister(qrCodeString: String) {
        val coronaTestQRCode = try {
            qrCodeValidator.validate(qrCodeString)
        } catch (err: InvalidQRCodeException) {
            Timber.i(err, "Failed to validate QRCode")
            qrCodeError.postValue(err)
            return
        }

        when {
            coronaTestQRCode.isDccSupportedByPoc && !coronaTestQRCode.isDccConsentGiven -> {
                SubmissionNavigationEvents.NavigateToRequestDccFragment(
                    coronaTestQRCode = coronaTestQRCode,
                    consentGiven = true,
                    allowReplacement = allowReplacement
                ).run { routeToScreen.postValue(this) }
            }
            else -> {
                registrationStateProcessor.startRegistration(
                    request = coronaTestQRCode,
                    isSubmissionConsentGiven = true,
                    allowReplacement = allowReplacement
                )
            }
        }
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDataPrivacy)
    }

    fun giveGoogleConsentResult(accepted: Boolean) {
        Timber.i("User allowed Google consent:$accepted")
        // Navigate regardless of consent result
        processQrCode(qrCode)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionConsentViewModel> {
        fun create(
            qrCode: String,
            allowReplacement: Boolean
        ): SubmissionConsentViewModel
    }
}
