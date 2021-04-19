package de.rki.coronawarnapp.ui.presencetracing.attendee.confirm

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.getDefaultAutoCheckoutLengthInMinutes
import de.rki.coronawarnapp.ui.durationpicker.toContactDiaryFormat
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.presencetracing.attendee.TraceLocationAttendeeSettings
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.mapTraceLocationToTitleRes
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class ConfirmCheckInViewModel @AssistedInject constructor(
    @Assisted private val verifiedTraceLocation: VerifiedTraceLocation,
    private val checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper,
    private val traceLocationAttendeeSettings: TraceLocationAttendeeSettings
) : CWAViewModel() {
    private val traceLocation = MutableStateFlow(verifiedTraceLocation.traceLocation)
    private val createJournalEntry = traceLocationAttendeeSettings.createJournalEntryCheckedState

    private val autoCheckOutLength = MutableStateFlow(
        Duration.standardMinutes(
            verifiedTraceLocation.traceLocation.getDefaultAutoCheckoutLengthInMinutes(timeStamper.nowUTC).toLong()
        )
    )

    val openDatePickerEvent = SingleLiveEvent<String>()
    val events = SingleLiveEvent<ConfirmCheckInNavigation>()

    val uiState = combine(
        traceLocation,
        createJournalEntry,
        autoCheckOutLength
    ) { traceLocation, createEntry, checkInLength ->
        UiState(
            traceLocation = traceLocation,
            createJournalEntry = createEntry,
            checkInEndOffset = checkInLength,
            eventInPastVisible = traceLocation.isAfterEndTime(timeStamper.nowUTC),
            eventInFutureVisible = traceLocation.isBeforeStartTime(timeStamper.nowUTC),
            confirmButtonEnabled = checkInLength.standardMinutes > 0
        )
    }.asLiveData()

    fun onClose() {
        events.value = ConfirmCheckInNavigation.BackNavigation
    }

    fun onConfirmTraceLocation() {
        launch {
            val now = timeStamper.nowUTC
            checkInRepository.addCheckIn(
                verifiedTraceLocation.toCheckIn(
                    checkInStart = now,
                    createJournalEntry = createJournalEntry.first(),
                    checkInEnd = now + autoCheckOutLength.value
                )
            )
            events.postValue(ConfirmCheckInNavigation.ConfirmNavigation)
        }
    }

    fun createJournalEntryToggled(state: Boolean) {
        traceLocationAttendeeSettings.setCreateJournalEntryCheckedState(state)
    }

    fun dateSelectorClicked() {
        openDatePickerEvent.value = autoCheckOutLength.value.toContactDiaryFormat()
    }

    fun durationUpdated(duration: Duration) {
        autoCheckOutLength.value = duration
    }

    private fun VerifiedTraceLocation.toCheckIn(
        checkInStart: Instant,
        checkInEnd: Instant,
        completed: Boolean = false,
        createJournalEntry: Boolean = true
    ): CheckIn {
        val traceLocation = verifiedTraceLocation.traceLocation
        return CheckIn(
            traceLocationId = traceLocation.locationId,
            version = traceLocation.version,
            type = traceLocation.type.number,
            description = traceLocation.description,
            address = traceLocation.address,
            traceLocationStart = traceLocation.startDate,
            traceLocationEnd = traceLocation.endDate,
            defaultCheckInLengthInMinutes = traceLocation.defaultCheckInLengthInMinutes,
            cryptographicSeed = traceLocation.cryptographicSeed,
            cnPublicKey = traceLocation.cnPublicKey,
            checkInStart = checkInStart,
            checkInEnd = checkInEnd,
            completed = completed,
            createJournalEntry = createJournalEntry,
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ConfirmCheckInViewModel> {
        fun create(
            verifiedTraceLocation: VerifiedTraceLocation
        ): ConfirmCheckInViewModel
    }

    data class UiState(
        private val traceLocation: TraceLocation,
        private val checkInEndOffset: Duration,
        val createJournalEntry: Boolean,
        val eventInPastVisible: Boolean,
        val eventInFutureVisible: Boolean,
        val confirmButtonEnabled: Boolean
    ) {
        val description get() = traceLocation.description
        val typeRes get() = mapTraceLocationToTitleRes(traceLocation.type)
        val address get() = traceLocation.address
        val checkInEnd get() = checkInEndOffset.toReadableDuration()
        val eventInFutureDateText get() = traceLocation.startDate?.toDateTime()?.toString(dateFormatter) ?: ""
        val eventInFutureTimeText get() = traceLocation.startDate?.toDateTime()?.toString(timeFormatter) ?: ""
    }
}

private val dateFormatter by lazy {
    DateTimeFormat.forPattern("EE, dd.MM.yy")
}

private val timeFormatter by lazy {
    DateTimeFormat.forPattern("HH:mm")
}
