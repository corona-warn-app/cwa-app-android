package de.rki.coronawarnapp.submission.ui.testresults.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.covidcertificate.test.CoronaTestRepository
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
    coronaTestRepository: CoronaTestRepository
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<RATResultNegativeNavigation>()
    val testAge = combine(
        intervalFlow(1),
        coronaTestRepository.coronaTests
    ) { _, tests ->
        val rapidTest = tests.firstOrNull {
            it.type == CoronaTest.Type.RAPID_ANTIGEN
        }

        rapidTest?.testAge()
    }.asLiveData(context = dispatcherProvider.Default)

    private fun CoronaTest.testAge(): TestAge? {
        if (this !is RACoronaTest) {
            Timber.d("Rapid test is missing")
            return null
        }

        val nowUTC = timeStamper.nowUTC
        val age = nowUTC.millis - testTakenAt.millis
        val ageText = formatter.print(Duration(age).toPeriod())

        return TestAge(test = this, ageText)
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

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATResultNegativeViewModel>

    data class TestAge(
        val test: RACoronaTest,
        val ageText: String,
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
