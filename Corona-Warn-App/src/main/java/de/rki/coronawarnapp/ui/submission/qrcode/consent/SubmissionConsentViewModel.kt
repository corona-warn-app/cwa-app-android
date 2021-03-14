package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.google.android.gms.common.api.ApiException
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider,
    private val tekHistoryProvider: TEKHistoryProvider,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val countries = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
        analyticsKeySubmissionCollector.reportAdvancedConsentGiven()
        launch {
            try {
                val preAuthorized = tekHistoryProvider.preAuthorizeExposureKeyHistory()
                // Routes to QR code screen either user has already granted permission or it is older Api
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
                Timber.i("Pre-authorized:$preAuthorized")
            } catch (exception: Exception) {
                if (exception is ApiException &&
                    exception.status.hasResolution()
                ) {
                    Timber.d(exception, "Pre-auth requires user resolution")
                    routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
                } else {
                    Timber.d(exception, "Pre-auth failed with unrecoverable exception")
                    routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
                }
            }
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
        // Navigate to QR code scan anyway regardless of consent result
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionConsentViewModel>
}
