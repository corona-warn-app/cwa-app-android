package de.rki.coronawarnapp.test.presencetracing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionPayload
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionRepository
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

class PresenceTracingTestViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    traceLocationRepository: TraceLocationRepository,
    checkInRepository: CheckInRepository,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val taskController: TaskController,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val traceWarningRepository: TraceWarningRepository,
    private val organizerSubmissionRepository: OrganizerSubmissionRepository,
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

    val error = SingleLiveEvent<Throwable>()
    val submissionResult = SingleLiveEvent<Boolean>()

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
        val overlaps = presenceTracingRiskRepository.allCheckInWarningOverlaps.first()
        val lastResult = presenceTracingRiskRepository.allRiskLevelResults.first().firstOrNull { it.wasSuccessfullyCalculated }

        val infoText = when {
            lastResult?.wasSuccessfullyCalculated != true -> "Last calculation failed"
            overlaps.isEmpty() -> "No matches found (${warningPackages.size} warning packages)."
            overlaps.size > 100 -> "Output too large. ${overlaps.size} lines"
            overlaps.isNotEmpty() -> overlaps.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                stringBuilder
                    .append("CheckIn Id ${checkInOverlap.checkInId}, ")
                    .append("Date ${checkInOverlap.localDateUtc}, ")
                    .append("Min. ${checkInOverlap.overlap.toMinutes()}")
                    .appendLine()
            }.toString()
            else -> "Unknown state"
        }
        presenceTracingWarningTaskResult.postValue(infoText)
    }

    fun resetProcessedWarningPackages() = launch {
        traceWarningRepository.reset()
    }

    fun runRiskCalculationPerCheckInDay() {
        launch {
            measureTime(
                {
                    Timber.d("Time to calculate risk: $it millis")
                    riskCalculationRuntime.postValue(it)
                },
                {
                    val checkInWarningOverlaps = presenceTracingRiskRepository.allCheckInWarningOverlaps.first()
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

    fun submit(traceLocation: TraceLocation, tan: String, startTime: String, duration: String) {
        launch {
            try {
                val localeDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))
                val startDate = localeDateTime.toInstant(ZoneOffset.UTC)
                val (hours, minutes) = duration.split(":")
                val parsedDuration = Duration.parse("PT${hours}H${minutes}M")

                val payload = OrganizerSubmissionPayload(
                    traceLocation = traceLocation,
                    tan = tan,
                    startDate = startDate,
                    endDate = startDate.plus(parsedDuration)
                )
                organizerSubmissionRepository.submit(payload)
                submissionResult.postValue(true)
            } catch (e: Exception) {
                Timber.d(e, "Organizer submission failed")
                error.postValue(e)
            }
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PresenceTracingTestViewModel>
}
