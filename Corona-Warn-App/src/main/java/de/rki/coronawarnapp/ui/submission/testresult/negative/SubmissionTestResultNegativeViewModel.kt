package de.rki.coronawarnapp.ui.submission.testresult.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

class SubmissionTestResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    certificateRepository: TestCertificateRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    @Assisted private val testType: BaseCoronaTest.Type,
    @Assisted private val testIdentifier: TestIdentifier
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val events = SingleLiveEvent<SubmissionTestResultNegativeNavigation>()
    val testResult = combine(
        submissionRepository.testForType(type = testType).filterNotNull(),
        certificateRepository.certificates
    ) { test, certs ->
        val cert = certs.firstOrNull {
            it.registrationToken == test.registrationToken
        }

        val certificateState: CertificateState = when (cert?.isCertificateRetrievalPending) {
            true -> CertificateState.PENDING
            false -> CertificateState.AVAILABLE
            else -> CertificateState.NOT_REQUESTED
        }

        UIState(
            coronaTest = test,
            certificateState = certificateState
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val certificate = combine(
        submissionRepository.testForType(type = testType).filterNotNull(),
        certificateRepository.certificates
    ) { test, certs ->
        val cert = certs.firstOrNull {
            it.registrationToken == test.registrationToken
        }

        cert
    }.asLiveData(context = dispatcherProvider.Default)

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        events.postValue(SubmissionTestResultNegativeNavigation.Back)
    }

    fun onTestOpened() = launch {
        Timber.tag(TAG).d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = testType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    fun onCertificateClicked() {
        certificate.value?.let {
            events.postValue(SubmissionTestResultNegativeNavigation.OpenTestCertificateDetails(it.containerId))
        }
    }

    enum class CertificateState {
        NOT_REQUESTED,
        PENDING,
        AVAILABLE
    }

    data class UIState(
        val coronaTest: BaseCoronaTest,
        val certificateState: CertificateState
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultNegativeViewModel> {
        fun create(testType: BaseCoronaTest.Type, testIdentifier: TestIdentifier): SubmissionTestResultNegativeViewModel
    }

    companion object {
        private const val TAG = "SubmissionTestResult:VM"
    }
}
