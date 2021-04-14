package de.rki.coronawarnapp.test.presencetracing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.system.measureTimeMillis

class PresenceTracingTestViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    traceLocationRepository: TraceLocationRepository,
    checkInRepository: CheckInRepository,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val taskController: TaskController,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val traceWarningRepository: TraceWarningRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val lastOrganiserLocation: LiveData<TraceLocation?> =
        traceLocationRepository.allTraceLocations
            .map { lastLocationData(it) }
            .asLiveData(dispatcherProvider.Default)

    val lastAttendeeLocation: LiveData<TraceLocation?> =
        checkInRepository.allCheckIns
            .map { lastAttendeeLocationData(it) }
            .asLiveData(dispatcherProvider.Default)

    val presenceTracingWarningTaskResult = MutableLiveData<String>()
    val taskRunTime = MutableLiveData<Long>()
    val riskCalculationRuntime = MutableLiveData<Long>()

    val checkInRiskPerDayText = MutableLiveData<String>()

    fun runPresenceTracingWarningTask() = launch {
        Timber.d("runWarningPackageTask()")
        presenceTracingWarningTaskResult.postValue("Running")
        taskRunTime.postValue(-1L)

        val duration = measureTimeMillis {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    PresenceTracingWarningTask::class,
                    originTag = "PresenceTracingTestViewModel"
                )
            )
        }
        taskRunTime.postValue(duration)

        val warningPackages = traceWarningRepository.allMetaData.first()
        val overlaps = presenceTracingRiskRepository.overlapsOfLast14DaysPlusToday.first()
        val lastResult = presenceTracingRiskRepository.latestEntries(1).first().singleOrNull()

        val infoText = when {
            !lastResult!!.wasSuccessfullyCalculated -> "Last calculation failed"
            overlaps.isEmpty() -> "No matches found (${warningPackages.size} warning packages)."
            overlaps.size > 100 -> "Output too large. ${overlaps.size} lines"
            overlaps.isNotEmpty() -> overlaps.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                stringBuilder
                    .append("CheckIn Id ${checkInOverlap.checkInId}, ")
                    .append("Date ${checkInOverlap.localDateUtc}, ")
                    .append("Min. ${checkInOverlap.overlap.standardMinutes}")
                    .appendLine()
            }.toString()
            else -> "Unknown state"
        }
        presenceTracingWarningTaskResult.postValue(infoText)
    }

    fun resetProcessedWarningPackages() = launch {
        traceWarningRepository.clear()
    }

    fun runRiskCalculationPerCheckInDay() {
        launch {
            measureTime(
                {
                    Timber.d("Time to calculate risk: $it millis")
                    riskCalculationRuntime.postValue(it)
                },
                {
                    val checkInWarningOverlaps = presenceTracingRiskRepository.overlapsOfLast14DaysPlusToday.first()
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

    private fun lastLocationData(it: List<TraceLocation>): TraceLocation? =
        it.maxByOrNull { traceLocation -> traceLocation.id }

    private fun lastAttendeeLocationData(it: List<CheckIn>): TraceLocation? {
        val checkIn = it.maxByOrNull { checkIn -> checkIn.id } ?: return null

        return TraceLocation(
            id = checkIn.id,
            type = TraceLocationOuterClass.TraceLocationType.forNumber(checkIn.type),
            description = checkIn.description,
            address = checkIn.address,
            startDate = checkIn.traceLocationStart,
            endDate = checkIn.traceLocationEnd,
            defaultCheckInLengthInMinutes = checkIn.defaultCheckInLengthInMinutes,
            cryptographicSeed = checkIn.cryptographicSeed,
            cnPublicKey = checkIn.cnPublicKey,
            version = checkIn.version
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PresenceTracingTestViewModel>
}
