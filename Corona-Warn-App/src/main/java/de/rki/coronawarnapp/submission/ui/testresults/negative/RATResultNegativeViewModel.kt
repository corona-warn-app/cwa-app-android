package de.rki.coronawarnapp.submission.ui.testresults.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import timber.log.Timber

class RATResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val submissionRepository: SubmissionRepository,
    coronaTestRepository: CoronaTestRepository,
    certificateRepository: TestCertificateRepository
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<RATResultNegativeNavigation>()
    val testAge = combine(
        intervalFlow(1),
        coronaTestRepository.coronaTests,
        certificateRepository.certificates
    ) { _, tests, certs ->
        val rapidTest = tests.firstOrNull {
            it.type == CoronaTest.Type.RAPID_ANTIGEN
        }

        val certificate = certs.firstOrNull {
            it.registrationToken == rapidTest?.registrationToken
        }

        rapidTest?.uiState(certificate)
    }.asLiveData(context = dispatcherProvider.Default)

    val certificate = combine(
        intervalFlow(1),
        coronaTestRepository.coronaTests,
        certificateRepository.certificates
    ) { _, tests, certs ->
        val rapidTest = tests.firstOrNull {
            it.type == CoronaTest.Type.RAPID_ANTIGEN
        }

        val certificate = certs.firstOrNull {
            it.registrationToken == rapidTest?.registrationToken
        }

        certificate
    }.asLiveData(context = dispatcherProvider.Default)

    private fun CoronaTest.uiState(certificate: TestCertificateWrapper?): UIState? {
        if (this !is RACoronaTest) {
            Timber.d("Rapid test is missing")
            return null
        }

        val nowUTC = timeStamper.nowUTC
        val age = nowUTC.millis - testTakenAt.millis
        val ageText = formatter.print(Duration(age).toPeriod())

        val certificateState: CertificateState = when (certificate?.isCertificateRetrievalPending) {
            true -> CertificateState.PENDING
            false -> CertificateState.AVAILABLE
            else -> CertificateState.NOT_REQUESTED
        }

        return UIState(
            test = this,
            ageText = ageText,
            certificateState = certificateState
        )
    }

    fun onDeleteTestConfirmed() {
        try {
            Timber.d("deleteTest")
            submissionRepository.removeTestFromDevice(CoronaTest.Type.RAPID_ANTIGEN)
            events.postValue(RATResultNegativeNavigation.Back)
        } catch (e: Exception) {
            Timber.d(e, "Failed to delete rapid antigen test")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    fun onDeleteTestClicked() {
        events.postValue(RATResultNegativeNavigation.ShowDeleteWarning)
    }

    fun onClose() {
        events.postValue(RATResultNegativeNavigation.Back)
    }

    fun onCertificateClicked() {
        certificate.value?.let {
            events.postValue(RATResultNegativeNavigation.OpenTestCertificateDetails(it.containerId))
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATResultNegativeViewModel>

    enum class CertificateState {
        NOT_REQUESTED,
        PENDING,
        AVAILABLE
    }

    data class UIState(
        val test: RACoronaTest,
        val ageText: String,
        val certificateState: CertificateState
    )

    companion object {
        private val formatter: PeriodFormatter =
            PeriodFormatterBuilder().apply {
                printZeroAlways()
                minimumPrintedDigits(2)
                appendHours()
                appendSuffix(":")
                appendMinutes()
                appendSuffix(":")
                appendSeconds()
            }.toFormatter()
    }
}
