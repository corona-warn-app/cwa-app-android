package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.events.TraceLocationId
import de.rki.coronawarnapp.eventregistration.events.TraceLocationUrl
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.toByteString
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository,
    private val checkInWarningMatcher: CheckInWarningMatcher,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val traceLocationId: TraceLocationId,
    private val traceLocationUrl: TraceLocationUrl,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val lastLocationData: LiveData<LastLocationData?> =
        traceLocationRepository.allTraceLocations
            .map { lastLocationData(it) }
            .asLiveData(dispatcherProvider.Default)

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
                    val matches = checkInWarningMatcher.execute()

                    checkInWarningOverlaps.addAll(matches)

                    if (checkInWarningOverlaps.size < 100) {
                        val text = checkInWarningOverlaps.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                            stringBuilder
                                .append("CheckIn Id ${checkInOverlap.checkInId}, ")
                                .append("Date ${checkInOverlap.localDateUtc}, ")
                                .append("Min. ${checkInOverlap.overlap.standardMinutes}")
                                .append("\n")
                        }
                        if (text.isBlank()) checkInOverlapsText.postValue("No matches found")
                        else checkInOverlapsText.postValue(text.toString())
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
                        if (text.isBlank()) checkInRiskPerDayText.postValue("No matches found")
                        else checkInRiskPerDayText.postValue(text.toString())
                    } else {
                        checkInRiskPerDayText.postValue("Output too large. ${riskStates.size} lines")
                    }
                }
            )
        }
    }

    private fun lastLocationData(it: List<TraceLocation>): LastLocationData? {
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}

data class LastLocationData(
    val traceLocation: TraceLocation,
    val id: String,
    val url: String
)
