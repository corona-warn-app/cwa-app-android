package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.google.android.gms.common.api.ApiException
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKResult
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
    private val tekHistoryProvider: TEKHistoryProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val countries = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
        launch {
            when (val result = tekHistoryProvider.preAuthorizedTemporaryExposureKeyHistory()) {
                is TEKResult.Error -> {
                    val exception = result.exception
                    if (exception is ApiException &&
                        exception.status.hasResolution()
                    ) {
                        Timber.e(exception, "Requires user resolution")
                        routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
                    } else {
                        Timber.e(exception, "Pre-auth failed with unrecoverable exception")
                        // TODO Handle other exceptions
                    }
                }
                // Routes to QR code screen either has already granted permission or it is older Api
                is TEKResult.Success -> routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
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
