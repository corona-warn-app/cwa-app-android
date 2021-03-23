package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.DummyCheckInPackage
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInOverlap
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.TraceLocationCheckInMatcher
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.findMatches
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import okio.ByteString
import org.joda.time.Instant
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationCheckInMatcher: TraceLocationCheckInMatcher,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val checkInOverlaps = mutableListOf<CheckInOverlap>()
    val checkInOverlapsText = MutableLiveData<String>()
    val matchingRuntime = MutableLiveData<Long>()
    val riskCalculationRuntime = MutableLiveData<Long>()

    val checkInRiskPerDayText = MutableLiveData<String>()

    fun runMatcher() {
        launch {
            measureTime(
                {
                    Timber.d("Time to find matches: $it millis")
                    matchingRuntime.postValue(it)
                },
                {
                    checkInOverlaps.clear()
                    checkInOverlaps.addAll((1..360).flatMap {
                        findMatches(checkIns, DummyCheckInPackage)
                    })
                    if (checkInOverlaps.size < 100) {
                        val text = checkInOverlaps.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                            stringBuilder
                                .append("CheckIn Id ${checkInOverlap.checkInId}, ")
                                .append("Date ${checkInOverlap.localDate}, ")
                                .append("Min. ${checkInOverlap.overlap.standardMinutes}")
                                .append("\n")

                        }
                        checkInOverlapsText.postValue(text.toString())
                    } else {
                        checkInOverlapsText.postValue("Output >= 100")
                    }
                }
            )
        }
    }

    fun runRiskCalculationPerCheckInDay() {
        launch {
            measureTime(
                {
                    Timber.d("Time to calculate risk: $it millis")
                    riskCalculationRuntime.postValue(it)
                },
                {
                    val normalizedTimePerCheckInDayList =
                        presenceTracingRiskCalculator.calculateNormalizedTime(checkInOverlaps)
                    val riskStates =
                        presenceTracingRiskCalculator.calculateRisk(normalizedTimePerCheckInDayList)

                    if (riskStates.size < 100) {
                        val text = riskStates.fold(StringBuilder()) { stringBuilder, checkInRiskPerDay ->
                            stringBuilder
                                .append("CheckIn Id ${checkInRiskPerDay.checkInId}, ")
                                .append("Date ${checkInRiskPerDay.localDate}, ")
                                .append("RiskState ${checkInRiskPerDay.riskState}")
                                .append("\n")
                        }
                        checkInRiskPerDayText.postValue(text.toString())
                    } else {
                        checkInRiskPerDayText.postValue("Output >= 100")
                    }
                }
            )
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}

val checkIns = (1L..100L).map {
    createCheckIn(
        id = it,
        traceLocationGuid = it.toString(),
        startDateStr = "2021-03-04T09:50+01:00",
        endDateStr = "2021-03-04T10:05:15+01:00"
    )
}

fun createCheckIn(
    id: Long = 1L,
    traceLocationGuid: String,
    startDateStr: String,
    endDateStr: String
) = CheckIn(
    id = id,
    guid = traceLocationGuid,
    version = 1,
    type = 2,
    description = "My birthday party",
    address = "Malibu",
    traceLocationStart = Instant.parse(startDateStr),
    traceLocationEnd = null,
    defaultCheckInLengthInMinutes = null,
    traceLocationBytes = ByteString.EMPTY,
    signature = ByteString.EMPTY,
    checkInStart = Instant.parse(startDateStr),
    checkInEnd = Instant.parse(endDateStr),
    completed = false,
    createJournalEntry = false
)
