package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.mapTraceLocationToTitleRes
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTime
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                checkInStartTime.value = startDateTime?.apply {
                    if (event.localTime != null) {
                        withHour(event.localTime.hour)
                        withMinute(event.localTime.minute)
                        withSecond(event.localTime.second)
                    }
                }?.toInstant(OffsetDateTime.now().offset)
            is DateTimePickerEvent.DatePickerEvent ->
                checkInStartTime.value = startDateTime?.apply {
                    if (event.localDate != null) {
                        withDayOfYear(event.localDate.dayOfYear)
                    }
                }?.toInstant(OffsetDateTime.now().offset)
        }
    }

    // TODO: improve
    fun onEndTimeChanged(event: DateTimePickerEvent) {
        val endDateTime = checkInEndTime.value?.toDateTime()

        when (event) {
            is DateTimePickerEvent.TimePickerEvent ->
                checkInEndTime.value = endDateTime?.apply {
                    if (event.localTime != null) {
                        withHour(event.localTime.hour)
                        withMinute(event.localTime.minute)
                        withSecond(event.localTime.second)
                    }
                }?.toInstant(OffsetDateTime.now().offset)
            is DateTimePickerEvent.DatePickerEvent ->
                checkInEndTime.value = endDateTime?.apply {
                    if (event.localDate != null) {
                        withDayOfYear(event.localDate.dayOfYear)
                    }
                }?.toInstant(OffsetDateTime.now().offset)
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
        val checkInStartDate: String get() = checkInStartInstant.toDateTime().format(dateFormatter)
        val checkInStartTime: String get() = checkInStartInstant.toDateTime().format(timeFormatter)
        val checkInEndDate: String get() = checkInEndInstant.toDateTime().format(dateFormatter)
        val checkInEndTime: String get() = checkInEndInstant.toDateTime().format(timeFormatter)
        val saveButtonEnabled: Boolean get() = isInputValid()
        val wrongInputErrorShown: Boolean get() = !saveButtonEnabled

        private fun isInputValid(): Boolean {
            val startBeforeEnd = checkInStartInstant.isBefore(checkInEndInstant)
            val lessThan24h = ChronoUnit.DAYS.between(checkInStartInstant, checkInEndInstant) < 1

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
