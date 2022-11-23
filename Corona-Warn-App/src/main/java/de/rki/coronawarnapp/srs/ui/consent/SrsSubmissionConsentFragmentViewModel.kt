package de.rki.coronawarnapp.srs.ui.consent

import android.app.Activity
import android.content.Intent
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SrsSubmissionConsentFragmentViewModel @AssistedInject constructor(
    @Assisted private val openTypeSelection: Boolean,
    private val checkInRepository: CheckInRepository,
    appConfigProvider: AppConfigProvider,
    dispatcherProvider: DispatcherProvider,
    tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory,
) : CWAViewModel(dispatcherProvider) {

    val showKeysRetrievalProgress = SingleLiveEvent<Boolean>()
    val showTracingConsentDialog = SingleLiveEvent<(Boolean) -> Unit>()
    val showPermissionRequest = SingleLiveEvent<(Activity) -> Unit>()
    val event = SingleLiveEvent<SrsSubmissionConsentNavigationEvents>()
    val timeBetweenSubmissionsInDays = appConfigProvider.currentConfig.map {
        it.selfReportSubmission.common.timeBetweenSubmissionsInDays
    }.asLiveData2()

    private val tekHistoryUpdater = tekHistoryUpdaterFactory.create(
        object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) = launch {
                onTekAvailable(teks)
            }

            override fun onTEKPermissionDeclined() {
                Timber.tag(TAG).d("onTEKPermissionDeclined")
                showKeysRetrievalProgress.postValue(false)
                // stay on screen
            }

            override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) {
                Timber.tag(TAG).d("onTracingConsentRequired")
                showKeysRetrievalProgress.postValue(false)
                showTracingConsentDialog.postValue(onConsentResult)
            }

            override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) {
                Timber.tag(TAG).d("onPermissionRequired")
                showKeysRetrievalProgress.postValue(false)
                showPermissionRequest.postValue(permissionRequest)
            }

            override fun onError(error: Throwable) {
                Timber.e(error, "Failed to update TEKs.")
                showKeysRetrievalProgress.postValue(false)
                error.report(
                    exceptionCategory = ExceptionCategory.EXPOSURENOTIFICATION,
                    prefix = TAG
                )
            }
        }
    )

    suspend fun onTekAvailable(teks: List<TemporaryExposureKey>) {
        Timber.tag(TAG).d("onTEKAvailable(teks.size=%d)", teks.size)
        showKeysRetrievalProgress.postValue(false)

        if (openTypeSelection) {
            Timber.tag(TAG).d("Navigate to TestType")
            event.postValue(SrsSubmissionConsentNavigationEvents.NavigateToTestType)
        } else {
            val completedCheckInsExist = checkInRepository.completedCheckIns.first().isNotEmpty()
            val navDirections = if (completedCheckInsExist) {
                Timber.tag(TAG).d("Navigate to ShareCheckins")
                SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins
            } else {
                Timber.tag(TAG).d("Navigate to ShareSymptoms")
                SrsSubmissionConsentNavigationEvents.NavigateToShareSymptoms
            }
            event.postValue(navDirections)
        }
    }

    fun onDataPrivacyClick() {
        event.postValue(SrsSubmissionConsentNavigationEvents.NavigateToDataPrivacy)
    }

    fun submissionConsentAcceptButtonClicked() {
        tekHistoryUpdater.getTeksOrRequestPermission()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showKeysRetrievalProgress.value = true
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    fun onConsentCancel() {
        event.postValue(SrsSubmissionConsentNavigationEvents.NavigateToMainScreen)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SrsSubmissionConsentFragmentViewModel> {
        fun create(
            openTypeSelection: Boolean
        ): SrsSubmissionConsentFragmentViewModel
    }

    companion object {
        private const val TAG = "SrsSubmissionConsentFragmentViewModel"
    }
}
