package de.rki.coronawarnapp.ui.submission.testresult.negative

import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
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
    certificateRepository: TestCertificateRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    @Assisted private val testType: CoronaTest.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()
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

    fun deregisterTestFromDevice() = launch {
        Timber.tag(TAG).d("deregisterTestFromDevice()")
        submissionRepository.removeTestFromDevice(type = testType)

        routeToScreen.postValue(null)
    }

    fun onTestOpened() = launch {
        Timber.tag(TAG).d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = testType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    enum class CertificateState {
        NOT_REQUESTED,
        PENDING,
        AVAILABLE
    }

    data class UIState(
        val coronaTest: CoronaTest,
        val certificateState: CertificateState
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultNegativeViewModel> {
        fun create(testType: CoronaTest.Type): SubmissionTestResultNegativeViewModel
    }

    companion object {
        private const val TAG = "SubmissionTestResult:VM"
    }
}
