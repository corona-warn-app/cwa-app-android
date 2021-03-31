package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.debug.measureTime
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import okio.ByteString.Companion.toByteString
import org.joda.time.DateTime
import timber.log.Timber
import java.util.UUID

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository,
    private val checkInWarningMatcher: CheckInWarningMatcher,
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

    fun generateTestTraceLocations() {
        val permanent = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
            description = "SAP Kantine WDF20",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(permanent)

        val oneDayEvent = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
            description = "Jahrestreffen der deutschen SAP Anwendergruppe (one day)",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = DateTime.now().plusHours(2).toInstant(),
            endDate = DateTime.now().plusHours(3).toInstant(),
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(oneDayEvent)

        val partyHardEvent = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
            description = "Jahrestreffen der deutschen SAP Anwendergruppe (many days)",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = DateTime.now().plusHours(2).toInstant(),
            endDate = DateTime.now().plusDays(5).plusHours(2).toInstant(),
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(partyHardEvent)

        val oldPermanent = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
            description = "SAP Kantine MOW07",
            address = "Moscow, Kosmodomianskaya 52/7",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Moscow, Kosmodomianskaya 52/7".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(oldPermanent)

        val oldTemporaryOne = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
            description = "Old temporary 1",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
            endDate = DateTime.now().minusSeconds(15 * 86400 - 10).toInstant(),
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(oldTemporaryOne)

        val oldTemporaryTwo = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
            description = "Old temporary 2",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
            endDate = DateTime.now().minusSeconds(15 * 86400).toInstant(),
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(oldTemporaryTwo)

        val oldTemporaryThree = TraceLocation(
            guid = UUID.randomUUID().toString(),
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
            description = "Old temporary 3",
            address = "Hauptstr. 3, 69115 Heidelberg",
            startDate = DateTime.now().minusSeconds(16 * 86400).toInstant(),
            endDate = DateTime.now().minusSeconds(15 * 86400 + 10).toInstant(),
            defaultCheckInLengthInMinutes = 60,
            byteRepresentation = "Hauptstr. 3, 69115 Heidelberg".toByteArray().toByteString(),
            signature = "Signature".toByteArray().toByteString()
        )
        traceLocationRepository.addTraceLocation(oldTemporaryThree)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
