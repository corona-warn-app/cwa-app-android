package de.rki.coronawarnapp.ui.submission.resultavailable

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultAvailableViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory,
    private val checkInRepository: CheckInRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    @Assisted private val testIdentifier: TestIdentifier,
    private val coronaTestProvider: CoronaTestProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen = SingleLiveEvent<NavDirections>()

    private val coronaTestFlow = coronaTestProvider.getTestForIdentifier(testIdentifier).filterNotNull()
    private val consentGivenFlow = coronaTestFlow
        .map {
            if (it is PersonalCoronaTest) {
                it.isAdvancedConsentGiven
            } else false
        }
    val consent = consentGivenFlow.asLiveData(dispatcherProvider.Default)
    val showPermissionRequest = SingleLiveEvent<(Activity) -> Unit>()
    val showCloseDialog = SingleLiveEvent<Unit>()
    val showKeysRetrievalProgress = SingleLiveEvent<Boolean>()
    val showTracingConsentDialog = SingleLiveEvent<(Boolean) -> Unit>()

    private val tekHistoryUpdater = tekHistoryUpdaterFactory.create(
        object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) = launch {
                Timber.tag(TAG).d("onTEKAvailable(teks.size=%d)", teks.size)
                showKeysRetrievalProgress.postValue(false)
                val completedCheckInsExist = checkInRepository.completedCheckIns.first().isNotEmpty()
                val navDirections = if (completedCheckInsExist) {
                    Timber.tag(TAG).d("Navigate to CheckInsConsentFragment")
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToCheckInsConsentFragment(
                            coronaTestFlow.first().type
                        )
                } else {
                    autoSubmission.updateMode(AutoSubmission.Mode.MONITOR)
                    Timber.tag(TAG).d("Navigate to SubmissionTestResultConsentGivenFragment")
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultConsentGivenFragment(
                            coronaTestFlow.first().type
                        )
                }
                routeToScreen.postValue(navDirections)
            }

            override fun onTEKPermissionDeclined() = launch {
                Timber.d("onTEKPermissionDeclined")
                showKeysRetrievalProgress.postValue(false)
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment(
                            testIdentifier
                        )
                )
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
                    prefix = "SubmissionTestResultAvailableViewModel"
                )
            }
        }
    )

    fun goBack() {
        showCloseDialog.postValue(Unit)
    }

    fun onCancelConfirmed() {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToMainFragment()
        )
    }

    fun goConsent() = launch {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(
                    testType = coronaTestFlow.first().type,
                    isTestResultAvailable = true
                )
        )
    }

    fun proceed() {
        launch {
            if (consentGivenFlow.first()) {
                showKeysRetrievalProgress.postValue(true)
                Timber.tag(TAG).d("tekHistoryUpdater.updateTEKHistoryOrRequestPermission")
                tekHistoryUpdater.getTeksOrRequestPermission()
            } else {
                Timber.tag(TAG).d("routeToScreen:SubmissionTestResultNoConsentFragment")
                coronaTestFlow.first().let {
                    if (it is PersonalCoronaTest)
                        analyticsKeySubmissionCollector.reportConsentWithdrawn(it.type)
                }
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment(
                            testIdentifier = testIdentifier
                        )
                )
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showKeysRetrievalProgress.value = true
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultAvailableViewModel> {
        fun create(testIdentifier: TestIdentifier): SubmissionTestResultAvailableViewModel
    }

    companion object {
        private const val TAG = "TestAvailableViewModel"
    }
}
