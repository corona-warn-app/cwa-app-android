package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultConsentGivenViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    // TODO Use navargs to supply this
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val showUploadDialog = autoSubmission.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

    val uiState: LiveData<TestResultUIState> = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onTestOpened() = launch {
        Timber.d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = coronaTestType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    fun onContinuePressed() {
        Timber.d("Beginning symptom flow")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onShowCancelDialog() {
        showCancelDialog.postValue(Unit)
    }

    fun onCancelConfirmed() {
        launch {
            try {
                autoSubmission.runSubmissionNow()
            } catch (e: Exception) {
                Timber.e(e, "onCancelConfirmed() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT)
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultConsentGivenViewModel>
}
