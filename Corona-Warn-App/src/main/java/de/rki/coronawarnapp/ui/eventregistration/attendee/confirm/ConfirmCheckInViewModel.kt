package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Duration
import org.joda.time.Instant

class ConfirmCheckInViewModel @AssistedInject constructor(
    @Assisted private val verifiedTraceLocation: VerifiedTraceLocation?,
    @Assisted private val editCheckInId: Long?,
    private val checkInRepository: CheckInRepository
) : CWAViewModel() {

    val events = SingleLiveEvent<ConfirmCheckInNavigation>()

    fun onClose() {
        events.value = ConfirmCheckInNavigation.BackNavigation
    }

    fun onConfirmTraceLocation() {
        if (verifiedTraceLocation == null) return
        launch {
            // TODO This is only for testing
            checkInRepository.addCheckIn(verifiedTraceLocation.toCheckIn(checkInStart = Instant.now()))
            events.postValue(ConfirmCheckInNavigation.ConfirmNavigation)
        }
    }

    private fun VerifiedTraceLocation.toCheckIn(
        checkInStart: Instant,
        checkInEnd: Instant = checkInStart.plus(
            Duration.standardMinutes(traceLocation.defaultCheckInLengthInMinutes?.toLong() ?: 3L)
        ),
        completed: Boolean = false,
        createJournalEntry: Boolean = true
    ): CheckIn = CheckIn(
        traceLocationBytes = traceLocationBytes,
        signature = signature,
        guid = traceLocation.guid,
        version = traceLocation.version,
        type = traceLocation.type.number,
        description = traceLocation.description,
        address = traceLocation.address,
        traceLocationStart = traceLocation.startDate,
        traceLocationEnd = traceLocation.endDate,
        defaultCheckInLengthInMinutes = traceLocation.defaultCheckInLengthInMinutes,
        checkInStart = checkInStart,
        checkInEnd = checkInEnd,
        completed = completed,
        createJournalEntry = createJournalEntry,
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ConfirmCheckInViewModel> {
        fun create(
            verifiedTraceLocation: VerifiedTraceLocation?,
            editCheckInId: Long?
        ): ConfirmCheckInViewModel
    }
}
