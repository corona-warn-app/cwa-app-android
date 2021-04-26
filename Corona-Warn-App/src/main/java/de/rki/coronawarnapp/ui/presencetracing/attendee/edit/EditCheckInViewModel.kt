package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.mapTraceLocationToTitleRes
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class EditCheckInViewModel @AssistedInject constructor(
    @Assisted private val editCheckInId: Long?,
    private val checkInRepository: CheckInRepository
) : CWAViewModel() {
    private val checkInFlow = MutableStateFlow<CheckIn?>(null)

    private val checkInStartTime = MutableStateFlow<Instant?>(null)
    private val checkInEndTime = MutableStateFlow<Instant?>(null)

    init {
        launch {
            val checkIn = checkInRepository.checkInForId(editCheckInId ?: 0)

            if (checkInStartTime.value == null) {
                checkInStartTime.value = checkIn.checkInStart.toDateTime().toInstant()
            }
            if (checkInEndTime.value == null) {
                checkInEndTime.value = checkIn.checkInEnd.toDateTime().toInstant()
            }

            checkInFlow.value = checkIn
        }
    }

    val openStartPickerEvent = SingleLiveEvent<DateTimePickerEvent>()
    val openEndPickerEvent = SingleLiveEvent<DateTimePickerEvent>()

    val events = SingleLiveEvent<EditCheckInNavigation>()

    val uiState = combine(
        checkInFlow.filterNotNull(),
        checkInStartTime.filterNotNull(),
        checkInEndTime.filterNotNull()
    ) { checkIn, checkInStartTime, checkInEndTime ->
        UiState(
            checkIn = checkIn,
            checkInStartInstant = checkInStartTime,
            checkInEndInstant = checkInEndTime
        )
    }.asLiveData()

    fun onClose() {
        events.value = EditCheckInNavigation.BackNavigation
    }

    fun onStartDateClicked() {
        val utcDateTime = checkInStartTime.value?.toDateTime()
        openStartPickerEvent.value = DateTimePickerEvent.DatePickerEvent(utcDateTime?.toLocalDate())
    }

    fun onStartTimeClicked() {
        val utcDateTime = checkInStartTime.value?.toDateTime()
        openStartPickerEvent.value = DateTimePickerEvent.TimePickerEvent(utcDateTime?.toLocalTime())
    }

    fun onEndDateClicked() {
        val utcDateTime = checkInEndTime.value?.toDateTime()
        openEndPickerEvent.value = DateTimePickerEvent.DatePickerEvent(utcDateTime?.toLocalDate())
    }

    fun onEndTimeClicked() {
        val utcDateTime = checkInEndTime.value?.toDateTime()
        openEndPickerEvent.value = DateTimePickerEvent.TimePickerEvent(utcDateTime?.toLocalTime())
    }

    fun onStartTimeChanged(event: DateTimePickerEvent) {
        val startDateTime = checkInStartTime.value?.toDateTime()

        when (event) {
            is DateTimePickerEvent.TimePickerEvent ->
                checkInStartTime.value = startDateTime?.withTime(event.localTime)?.toInstant()
            is DateTimePickerEvent.DatePickerEvent ->
                checkInStartTime.value = startDateTime?.withDate(event.localDate)?.toInstant()
        }
    }

    fun onEndTimeChanged(event: DateTimePickerEvent) {
        val endDateTime = checkInEndTime.value?.toDateTime()

        when (event) {
            is DateTimePickerEvent.TimePickerEvent ->
                checkInEndTime.value = endDateTime?.withTime(event.localTime)?.toInstant()
            is DateTimePickerEvent.DatePickerEvent ->
                checkInEndTime.value = endDateTime?.withDate(event.localDate)?.toInstant()
        }
    }

    fun onSaveClicked() {
        launch {
            val oldCheckIn = checkInFlow.value
            val newCheckInTime = checkInStartTime.value
            val newCheckOutTime = checkInEndTime.value

            if (oldCheckIn != null && newCheckInTime != null && newCheckOutTime != null) {
                checkInRepository.updateCheckIn(checkInId = oldCheckIn.id) {
                    it.copy(
                        checkInStart = newCheckInTime,
                        checkInEnd = newCheckOutTime
                    )
                }
            }
            events.postValue(EditCheckInNavigation.ConfirmNavigation)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<EditCheckInViewModel> {
        fun create(
            editCheckInId: Long?
        ): EditCheckInViewModel
    }

    data class UiState(
        private val checkIn: CheckIn,
        private val checkInStartInstant: Instant,
        private val checkInEndInstant: Instant
    ) {
        val description: String get() = checkIn.description
        @get:StringRes val typeRes: Int get() = mapTraceLocationToTitleRes(checkIn.type)
        val address: String get() = checkIn.address
        val diaryWarningVisible: Boolean get() = checkIn.createJournalEntry
        val checkInStartDate: String get() = checkInStartInstant.toDateTime().toString(dateFormatter)
        val checkInStartTime: String get() = checkInStartInstant.toDateTime().toString(timeFormatter)
        val checkInEndDate: String get() = checkInEndInstant.toDateTime().toString(dateFormatter)
        val checkInEndTime: String get() = checkInEndInstant.toDateTime().toString(timeFormatter)
        val saveButtonEnabled: Boolean get() = isInputValid()
        val wrongInputErrorShown: Boolean get() = !saveButtonEnabled

        private fun isInputValid(): Boolean {
            val startBeforeEnd = checkInStartInstant.isBefore(checkInEndInstant)
            val lessThan24h = Days.daysBetween(checkInStartInstant, checkInEndInstant).days < 1

            return startBeforeEnd and lessThan24h
        }
    }

    sealed class DateTimePickerEvent {
        open class DatePickerEvent(val localDate: LocalDate?) : DateTimePickerEvent()
        open class TimePickerEvent(val localTime: LocalTime?) : DateTimePickerEvent()
    }
}

private val dateFormatter by lazy {
    DateTimeFormat.forPattern("EE, dd.MM.yy")
}

private val timeFormatter by lazy {
    DateTimeFormat.forPattern("HH:mm")
}
