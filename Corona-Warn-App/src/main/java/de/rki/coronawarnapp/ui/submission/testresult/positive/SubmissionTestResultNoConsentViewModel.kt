package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultNoConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel() {
    // TODO Use navargs to supply this
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val uiState: LiveData<TestResultUIState> = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    fun onTestOpened() = launch {
        Timber.v("onTestOpened()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT)
        submissionRepository.setViewedTestResult(type = coronaTestType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultNoConsentViewModel>
}
