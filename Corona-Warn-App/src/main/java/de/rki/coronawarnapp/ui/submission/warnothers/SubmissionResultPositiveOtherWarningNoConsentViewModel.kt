package de.rki.coronawarnapp.ui.submission.warnothers

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionResultPositiveOtherWarningNoConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    private val tekHistoryUpdater: TEKHistoryUpdater,
    private val interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<NavDirections>()

    val showPermissionRequest = SingleLiveEvent<(Activity) -> Unit>()

    val showEnableTracingEvent = SingleLiveEvent<Unit>()

    val countryList = interoperabilityRepository.countryListFlow.asLiveData()

    init {
        tekHistoryUpdater.callback = object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                routeToScreen.postValue(
                    SubmissionResultPositiveOtherWarningNoConsentFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningNoConsentFragmentToSubmissionResultReadyFragment()
                )
            }

            override fun onPermissionDeclined() {
                routeToScreen.postValue(
                    SubmissionResultPositiveOtherWarningNoConsentFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningNoConsentFragmentToMainFragment()
                )
            }

            override fun onError(error: Throwable) {
                Timber.e(error, "Couldn't access temporary exposure key history.")
                error.report(ExceptionCategory.EXPOSURENOTIFICATION, "Failed to obtain TEKs.")
            }
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(
            SubmissionResultPositiveOtherWarningNoConsentFragmentDirections
                .actionSubmissionResultPositiveOtherWarningNoConsentFragmentToMainFragment()
        )
    }

    fun onConsentButtonClicked() {
        launch {
            if (enfClient.isTracingEnabled.first()) {
                tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
                    showPermissionRequest.postValue(permissionRequest)
                }
            } else {
                showEnableTracingEvent.postValue(Unit)
            }
        }
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(
            SubmissionResultPositiveOtherWarningNoConsentFragmentDirections
                .actionSubmissionResultPositiveOtherWarningNoConsentFragmentToInformationPrivacyFragment()
        )
    }

    fun handleActivityRersult(requestCode: Int, resultCode: Int, data: Intent?) {
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionResultPositiveOtherWarningNoConsentViewModel> {
        fun create(): SubmissionResultPositiveOtherWarningNoConsentViewModel
    }
}
