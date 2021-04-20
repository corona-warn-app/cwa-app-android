package de.rki.coronawarnapp.ui.submission.resultavailable

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultAvailableViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory,
    submissionRepository: SubmissionRepository,
    private val checkInRepository: CheckInRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    // TODO Use navargs to supply this?
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    val routeToScreen = SingleLiveEvent<NavDirections>()

    private val consentFlow = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .map { it.isAdvancedConsentGiven }
    val consent = consentFlow.asLiveData(dispatcherProvider.Default)
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
                        .actionSubmissionTestResultAvailableFragmentToCheckInsConsentFragment()
                } else {
                    autoSubmission.updateMode(AutoSubmission.Mode.MONITOR)
                    Timber.tag(TAG).d("Navigate to SubmissionTestResultConsentGivenFragment")
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultConsentGivenFragment()
                }
                routeToScreen.postValue(navDirections)
            }

            override fun onTEKPermissionDeclined() {
                Timber.d("onTEKPermissionDeclined")
                showKeysRetrievalProgress.postValue(false)
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
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

    init {
        submissionRepository.refreshTest(type = CoronaTest.Type.PCR)
    }

    fun goBack() {
        showCloseDialog.postValue(Unit)
    }

    fun onCancelConfirmed() {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToMainFragment()
        )
    }

    fun goConsent() {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(
                    isTestResultAvailable = true
                )
        )
    }

    fun proceed() {
        showKeysRetrievalProgress.value = true
        launch {
            if (consentFlow.first()) {
                Timber.tag(TAG).d("tekHistoryUpdater.updateTEKHistoryOrRequestPermission")
                tekHistoryUpdater.updateTEKHistoryOrRequestPermission()
            } else {
                Timber.tag(TAG).d("routeToScreen:SubmissionTestResultNoConsentFragment")
                analyticsKeySubmissionCollector.reportConsentWithdrawn()
                showKeysRetrievalProgress.postValue(false)
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
                )
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showKeysRetrievalProgress.value = true
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultAvailableViewModel>

    companion object {
        private const val TAG = "TestAvailableViewModel"
    }
}
