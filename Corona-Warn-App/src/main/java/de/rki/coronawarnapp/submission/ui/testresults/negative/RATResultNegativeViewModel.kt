package de.rki.coronawarnapp.submission.ui.testresults.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
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
import java.lang.Exception

class RATResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider,
    private val timeStamper: TimeStamper,
    private val submissionRepository: SubmissionRepository,
    coronaTestRepository: CoronaTestRepository
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<RATResultNegativeNavigation>()
    val testAge = combine(
        intervalFlow(1),
        coronaTestRepository.coronaTests
    ) { _, tests ->
        val rapidTest = tests.firstOrNull { it.type == CoronaTest.Type.RAPID_ANTIGEN }
        testAge(rapidTest)
    }.asLiveData(context = dispatcherProvider.Default)

    private suspend fun testAge(rapidTest: CoronaTest?): TestAge? {
        if (rapidTest !is RACoronaTest) {
            Timber.d("Rapid test is missing")
            return null
        }

        // TODO
        val hours = appConfigProvider.getAppConfig()
            .coronaTestParameters
            .coronaRapidAntigenTestParameters
            .hoursToDeemTestOutdated

        val duration = Duration(rapidTest.testedAt, timeStamper.nowUTC)
        val ageText = formatter.print(duration.toPeriod())

        return TestAge(test = rapidTest, ageText)
    }

    fun deleteTest() {
        try {
            Timber.d("deleteTest")
            submissionRepository.removeTestFromDevice(CoronaTest.Type.RAPID_ANTIGEN)
            events.postValue(RATResultNegativeNavigation.Back)
        } catch (e: Exception) {
            Timber.d(e, "Failed to delete rapid antigen test")
            e.report(ExceptionCategory.INTERNAL)
        }
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
        private val formatter: PeriodFormatter = PeriodFormatterBuilder().apply {
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
