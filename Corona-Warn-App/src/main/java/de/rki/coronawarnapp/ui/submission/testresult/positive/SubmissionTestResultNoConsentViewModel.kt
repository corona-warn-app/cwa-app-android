package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultNoConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    @Assisted private val testIdentifier: TestIdentifier,
    private val coronaTestProvider: CoronaTestProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    init {
        Timber.v("init() coronaTestIdentifier=%s", testIdentifier)
    }

    private val coronaTestFlow = coronaTestProvider.findTestById(testIdentifier = testIdentifier)
        .filterNotNull()

    val uiState: LiveData<TestResultUIState> = coronaTestFlow
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    fun onTestOpened() = launch {
        Timber.v("onTestOpened()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, coronaTestFlow.first().type)
        coronaTestProvider.setTestAsViewed(coronaTestFlow.first())
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultNoConsentViewModel> {
        fun create(testIdentifier: TestIdentifier): SubmissionTestResultNoConsentViewModel
    }
}
