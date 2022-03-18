package de.rki.coronawarnapp.ui.submission.qrcode.consent

import com.google.android.gms.common.api.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
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
    dispatcherProvider: DispatcherProvider,
    interoperabilityRepository: InteroperabilityRepository,
    @Assisted private val coronaTestQRCode: CoronaTestQRCode,
    @Assisted private val allowReplacement: Boolean,
    private val tekHistoryProvider: TEKHistoryProvider,
    private val registrationStateProcessor: TestRegistrationStateProcessor
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val qrCodeError = SingleLiveEvent<Exception>()
    val registrationState = registrationStateProcessor.state.asLiveData2()
    val countries = interoperabilityRepository.countryList.asLiveData2()

    fun onConsentButtonClick() = launch {
        try {
            val preAuthorized = tekHistoryProvider.preAuthorizeExposureKeyHistory()
            // Proceed anyway, either user has already granted permission or it is older Api
            register(coronaTestQRCode)
            Timber.i("Pre-authorized:$preAuthorized")
        } catch (exception: Exception) {
            if (exception is ApiException &&
                exception.status.hasResolution()
            ) {
                Timber.d(exception, "Pre-auth requires user resolution")
                routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
            } else {
                Timber.d(exception, "Pre-auth failed with unrecoverable exception")
                register(coronaTestQRCode)
            }
        }
    }

    private suspend fun register(coronaTestQRCode: CoronaTestQRCode) {
        when {
            coronaTestQRCode.isDccSupportedByPoc && !coronaTestQRCode.isDccConsentGiven -> {
                SubmissionNavigationEvents.NavigateToRequestDccFragment(
                    coronaTestQRCode = coronaTestQRCode,
                    consentGiven = true,
                    allowReplacement = allowReplacement
                ).run { routeToScreen.postValue(this) }
            }
            else -> {
                registrationStateProcessor.startTestRegistration(
                    request = coronaTestQRCode,
                    isSubmissionConsentGiven = true,
                    allowTestReplacement = allowReplacement
                )
            }
        }
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDataPrivacy)
    }

    fun giveGoogleConsentResult(accepted: Boolean) = launch {
        Timber.i("User allowed Google consent:$accepted")
        // Navigate regardless of consent result
        register(coronaTestQRCode)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionConsentViewModel> {
        fun create(
            coronaTestQRCode: CoronaTestQRCode,
            allowReplacement: Boolean
        ): SubmissionConsentViewModel
    }
}
