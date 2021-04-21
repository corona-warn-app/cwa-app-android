package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.google.android.gms.common.api.ApiException
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeSubmission
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionConsentViewModel @AssistedInject constructor(
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider,
    private val tekHistoryProvider: TEKHistoryProvider,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val qrCodeSubmission: QrCodeSubmission
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = qrCodeSubmission.showRedeemedTokenWarning
    val qrCodeValidationState = qrCodeSubmission.qrCodeValidationState
    val registrationState = qrCodeSubmission.registrationState
    val registrationError = qrCodeSubmission.registrationError

    val countries = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    var qrCode: String? = null

    fun onConsentButtonClick() {
        // TODO Do we have a Test registered at this time? We need to forward the decision with navargs?
//        submissionRepository.giveConsentToSubmission(type = CoronaTest.Type.PCR)
        analyticsKeySubmissionCollector.reportAdvancedConsentGiven()
        launch {
            try {
                val preAuthorized = tekHistoryProvider.preAuthorizeExposureKeyHistory()
                // Routes to QR code screen either user has already granted permission or it is older Api
                proceed()
                Timber.i("Pre-authorized:$preAuthorized")
            } catch (exception: Exception) {
                if (exception is ApiException &&
                    exception.status.hasResolution()
                ) {
                    Timber.d(exception, "Pre-auth requires user resolution")
                    routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
                } else {
                    Timber.d(exception, "Pre-auth failed with unrecoverable exception")
                    proceed()
                }
            }
        }
    }

    private fun proceed() {
        qrCode.let {
            if (it == null)
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
            else
                processQrCode(it)
        }
    }

    private fun processQrCode(rawResult: String) {
        launch {
            qrCodeSubmission.startQrCodeRegistration(rawResult)
        }
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDataPrivacy)
    }

    fun giveGoogleConsentResult(accepted: Boolean) {
        Timber.i("User allowed Google consent:$accepted")
        // Navigate regardless of consent result
        proceed()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionConsentViewModel>
}
