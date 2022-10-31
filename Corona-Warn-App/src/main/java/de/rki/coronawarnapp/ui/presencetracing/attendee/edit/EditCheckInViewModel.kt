package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.mapTraceLocationToTitleRes
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
                checkInStartTime.value = checkIn.checkInStart
            }
            if (checkInEndTime.value == null) {
                checkInEndTime.value = checkIn.checkInEnd
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
        val dateTime = checkInStartTime.value?.toLocalDateTimeUserTz()
        openStartPickerEvent.value = DateTimePickerEvent.DatePickerEvent(dateTime?.toLocalDate())
    }

    fun onStartTimeClicked() {
        val dateTime = checkInStartTime.value?.toLocalDateTimeUserTz()
        openStartPickerEvent.value = DateTimePickerEvent.TimePickerEvent(dateTime?.toLocalTime())
    }

    fun onEndDateClicked() {
        val dateTime = checkInEndTime.value?.toLocalDateTimeUserTz()
        openEndPickerEvent.value = DateTimePickerEvent.DatePickerEvent(dateTime?.toLocalDate())
    }

    fun onEndTimeClicked() {
        val dateTime = checkInEndTime.value?.toLocalDateTimeUserTz()
        openEndPickerEvent.value = DateTimePickerEvent.TimePickerEvent(dateTime?.toLocalTime())
    }

    fun onStartTimeChanged(event: DateTimePickerEvent) {
        val startDateTime = checkInStartTime.value?.toLocalDateTimeUserTz()

        when (event) {
            is DateTimePickerEvent.TimePickerEvent ->
                checkInStartTime.value =
                    startDateTime?.toLocalDate()?.atTime(event.localTime)?.atZone(ZoneOffset.systemDefault())
                        ?.toInstant()
            is DateTimePickerEvent.DatePickerEvent ->
                checkInStartTime.value =
                    startDateTime?.toLocalTime()?.atDate(event.localDate)?.atZone(ZoneOffset.systemDefault())
                        ?.toInstant()
        }
    }

    fun onEndTimeChanged(event: DateTimePickerEvent) {
        val endDateTime = checkInEndTime.value?.toLocalDateTimeUserTz()

        when (event) {
            is DateTimePickerEvent.TimePickerEvent ->
                checkInEndTime.value =
                    endDateTime?.toLocalDate()?.atTime(event.localTime)?.atZone(ZoneOffset.systemDefault())?.toInstant()
            is DateTimePickerEvent.DatePickerEvent ->
                checkInEndTime.value =
                    endDateTime?.toLocalTime()?.atDate(event.localDate)?.atZone(ZoneOffset.systemDefault())?.toInstant()
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
        val checkInStartDate: String get() = checkInStartInstant.toLocalDateTimeUserTz().format(dateFormatter)
        val checkInStartTime: String get() = checkInStartInstant.toLocalDateTimeUserTz().format(timeFormatter)
        val checkInEndDate: String get() = checkInEndInstant.toLocalDateTimeUserTz().format(dateFormatter)
        val checkInEndTime: String get() = checkInEndInstant.toLocalDateTimeUserTz().format(timeFormatter)
        val saveButtonEnabled: Boolean get() = isInputValid()
        val wrongInputErrorShown: Boolean get() = !saveButtonEnabled

        private fun isInputValid(): Boolean {
            val startBeforeEnd = checkInStartInstant.isBefore(checkInEndInstant)
            val lessThan24h = Duration.between(checkInStartInstant, checkInEndInstant).toDays() < 1

            return startBeforeEnd and lessThan24h
        }
    }

    sealed class DateTimePickerEvent {
        open class DatePickerEvent(val localDate: LocalDate?) : DateTimePickerEvent()
        open class TimePickerEvent(val localTime: LocalTime?) : DateTimePickerEvent()
    }
}

private val dateFormatter by lazy {
    DateTimeFormatter.ofPattern("EE, dd.MM.yy")
}

private val timeFormatter by lazy {
    DateTimeFormatter.ofPattern("HH:mm")
}
