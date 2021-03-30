package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInMatcher
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInOverlap
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import okio.ByteString.Companion.toByteString
import org.joda.time.DateTime
import java.util.UUID

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val checkInMatcher: CheckInMatcher,
    private val traceLocationRepository: TraceLocationRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val checkInOverlaps = MutableLiveData<List<CheckInOverlap>>()

    fun runMatcher() {
        launch {
            val overlaps = checkInMatcher.execute()
            checkInOverlaps.postValue(overlaps)
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
