package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultNoConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    @Assisted private val testType: CoronaTest.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val uiState: LiveData<TestResultUIState> = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    fun onTestOpened() = launch {
        Timber.v("onTestOpened()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, testType)
        submissionRepository.setViewedTestResult(type = testType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultNoConsentViewModel> {
        fun create(testType: CoronaTest.Type): SubmissionTestResultNoConsentViewModel
    }
}
