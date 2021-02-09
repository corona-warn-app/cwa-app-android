package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SubmissionConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider,
    private val exposureNotificationClient: ExposureNotificationClient
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val countries = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
        launch {
            try {
                Timber.i("hit:requestPreAuthorizedTemporaryExposureKeyHistory")
                exposureNotificationClient.requestPreAuthorizedTemporaryExposureKeyHistory().await()
                Timber.i("Pre-auth is enabled")
            } catch (exception: Exception) {
                Timber.e(exception)
                when (exception) {
                    is ApiException ->
                        if (exception.status.hasResolution()) {
                            Timber.e("Requires user resolution (code: ${exception.statusCode})")
                            routeToScreen.postValue(SubmissionNavigationEvents.ResolvePlayServicesException(exception))
                        } else {
                            Timber.e("Pre-auth failed with unrecoverable exception: ${exception.message}")
                        }

                    else -> Timber.e("Pre-auth failed with unrecoverable exception: ${exception.message}")

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
