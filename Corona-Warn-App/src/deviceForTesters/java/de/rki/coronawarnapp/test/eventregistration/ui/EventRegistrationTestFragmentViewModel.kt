package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
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
import okio.ByteString.Companion.encode
import org.joda.time.DateTime
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository,
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val taskController: TaskController,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val traceWarningRepository: TraceWarningRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val presenceTracingWarningTaskResult = MutableLiveData<String>()
    val taskRunTime = MutableLiveData<Long>()
    val riskCalculationRuntime = MutableLiveData<Long>()

    val checkInRiskPerDayText = MutableLiveData<String>()

    fun runPresenceTracingWarningTask() = launch {
        Timber.d("runWarningPackageTask()")
        presenceTracingWarningTaskResult.postValue("")
        taskRunTime.postValue(-1L)

        val start = System.currentTimeMillis()
        taskController.submitBlocking(
            DefaultTaskRequest(
                PresenceTracingWarningTask::class,
                originTag = "EventRegistrationTestFragmentViewModel"
            )
        )
        val stop = System.currentTimeMillis()
        taskRunTime.postValue(stop - start)

        val warningPackages = traceWarningRepository.allMetaData.first()
        val overlaps = presenceTracingRiskRepository.checkInWarningOverlaps.first()
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
                    .append("\n")
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
                    val checkInWarningOverlaps = presenceTracingRiskRepository.checkInWarningOverlaps.first()
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

    fun generateTestTraceLocations() {
        launch {
            val permanent = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
                description = "SAP Kantine WDF20",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(permanent)

            val oneDayEvent = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
                description = "Jahrestreffen der deutschen SAP Anwendergruppe (one day)",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = DateTime.now().plusHours(2).toInstant(),
                endDate = DateTime.now().plusHours(3).toInstant(),
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(oneDayEvent)

            val partyHardEvent = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
                description = "Jahrestreffen der deutschen SAP Anwendergruppe (many days)",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = DateTime.now().plusHours(2).toInstant(),
                endDate = DateTime.now().plusDays(5).plusHours(2).toInstant(),
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(partyHardEvent)

            val oldPermanent = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
                description = "SAP Kantine MOW07",
                address = "Moscow, Kosmodomianskaya 52/7",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(oldPermanent)

            val oldTemporaryOne = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
                description = "Old temporary 1",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
                endDate = DateTime.now().minusSeconds(15 * 86400 - 10).toInstant(),
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(oldTemporaryOne)

            val oldTemporaryTwo = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
                description = "Old temporary 2",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
                endDate = DateTime.now().minusSeconds(15 * 86400).toInstant(),
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(oldTemporaryTwo)

            val oldTemporaryThree = TraceLocation(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
                description = "Old temporary 3",
                address = "Hauptstr. 3, 69115 Heidelberg",
                startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
                endDate = DateTime.now().minusSeconds(15 * 86400 + 10).toInstant(),
                defaultCheckInLengthInMinutes = 60,
                cryptographicSeed = "".encode(),
                cnPublicKey = ""
            )
            traceLocationRepository.addTraceLocation(oldTemporaryThree)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
