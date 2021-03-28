package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.DummyCheckInPackage
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.presencetracing.risk.launchMatching
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.awaitAll
import okio.ByteString
import org.joda.time.Instant
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val checkInWarningOverlaps = mutableListOf<CheckInWarningOverlap>()
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
                    checkInWarningOverlaps.clear()
                    val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }
                    val warningPackages = listOf(1..360).map {
                        DummyCheckInPackage
                    }
                    val matches = launchMatching(splitCheckIns, warningPackages, dispatcherProvider.IO)
                        .awaitAll()
                        .flatten()

                    checkInWarningOverlaps.addAll(matches)

                    if (checkInWarningOverlaps.size < 100) {
                        val text = checkInWarningOverlaps.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                            stringBuilder
                                .append("CheckIn Id ${checkInOverlap.checkInId}, ")
                                .append("Date ${checkInOverlap.localDateUtc}, ")
                                .append("Min. ${checkInOverlap.overlap.standardMinutes}")
                                .append("\n")
                        }
                        checkInOverlapsText.postValue(text.toString())
                    } else {
                        checkInOverlapsText.postValue("Output too large. ${checkInWarningOverlaps.size} lines")
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
                        presenceTracingRiskCalculator.calculateNormalizedTime(checkInWarningOverlaps)
                    val riskStates =
                        presenceTracingRiskCalculator.calculateCheckInRiskPerDay(normalizedTimePerCheckInDayList)

                    if (riskStates.size < 100) {
                        val text = riskStates.fold(StringBuilder()) { stringBuilder, checkInRiskPerDay ->
                            stringBuilder
                                .append("CheckIn Id ${checkInRiskPerDay.checkInId}, ")
                                .append("Date ${checkInRiskPerDay.localDateUtc}, ")
                                .append("RiskState ${checkInRiskPerDay.riskState}")
                                .append("\n")
                        }
                        checkInRiskPerDayText.postValue(text.toString())
                    } else {
                        checkInRiskPerDayText.postValue("Output too large. ${riskStates.size} lines")
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
