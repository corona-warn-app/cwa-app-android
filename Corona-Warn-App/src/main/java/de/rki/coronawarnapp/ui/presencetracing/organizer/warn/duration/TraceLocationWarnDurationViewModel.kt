package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class TraceLocationWarnDurationViewModel @AssistedInject constructor(
    @Assisted private val traceLocation: TraceLocation,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val uiState = MutableStateFlow(UiState())
    val state: LiveData<UiState> = uiState.asLiveData2()
    val selectedDate: LocalDate get() = uiState.value.localDate
    val selectedTime: LocalTime get() = uiState.value.localTime
    val selectedDuration: Duration get() = uiState.value.duration

    init {
        updateUiState()
    }

    fun dateChanged(localDate: LocalDate, localTime: LocalTime) {
        val resultTime = if (localDate == LocalDate.now() &&
            localTime.isAfter(LocalTime.now())
        ) LocalTime.now() else localTime
        uiState.apply { value = value.copy(localDate = localDate, localTime = resultTime) }
    }

    fun durationChanged(duration: Duration) {
        uiState.apply {
            value = value.copy(
                duration = Duration.standardMinutes(duration.standardMinutes.coerceIn(15, 1425))
            )
        }
    }

    private fun updateUiState() {
        uiState.apply {
            value = value.copy(
                description = traceLocation.description,
                startDateTime = traceLocation.startDate,
                endDateTime = traceLocation.endDate
            )
        }

        if (traceLocation.startDate != null &&
            traceLocation.startDate != Instant.EPOCH &&
            !traceLocation.isBeforeStartTime(timeStamper.nowUTC)
        ) {
            uiState.apply {
                value = value.copy(
                    localDate = traceLocation.startDate.toDateTime().toLocalDate(),
                    localTime = traceLocation.startDate.toDateTime().toLocalTime()
                )
            }
        }

        val duration = if (traceLocation.endDate != null && traceLocation.endDate != Instant.EPOCH) {
            getNearestFifteen(Duration(traceLocation.startDate, traceLocation.endDate).standardMinutes)
        } else if (traceLocation.defaultCheckInLengthInMinutes != null &&
            traceLocation.defaultCheckInLengthInMinutes > 0
        ) {
            getNearestFifteen(traceLocation.defaultCheckInLengthInMinutes.toLong())
        } else {
            Duration.standardHours(2)
        }
        durationChanged(duration)
    }

    private fun getNearestFifteen(durationMinutes: Long): Duration {
        val minDif = durationMinutes % 15
        val maxDif = 15 - (durationMinutes % 15)
        return Duration.standardMinutes(if (minDif < maxDif) durationMinutes - minDif else durationMinutes + maxDif)
    }

    data class UiState(
        val description: String? = null,
        val startDateTime: Instant? = null,
        val endDateTime: Instant? = null,
        val localDate: LocalDate = LocalDate.now(),
        val localTime: LocalTime = LocalTime.now(),
        val duration: Duration = Duration.standardMinutes(15)
    ) {
        fun formattedDateTime() = "${localDate.toDayFormat()} ${localTime.toShortTimeFormat()}"
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationWarnDurationViewModel> {
        fun create(
            traceLocation: TraceLocation
        ): TraceLocationWarnDurationViewModel
    }
}
