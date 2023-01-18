package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import android.content.res.Resources
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.TraceLocationWarnDuration
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

class TraceLocationWarnDurationViewModel @AssistedInject constructor(
    @Assisted private val traceLocation: TraceLocation,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val uiState = MutableStateFlow(UiState())
    val state: LiveData<UiState> = uiState.asLiveData2()
    val selectedDateTime: LocalDateTime get() = uiState.value.localDateTime
    val selectedDuration: Duration get() = uiState.value.duration
    val events = SingleLiveEvent<TraceLocationWarnDurationEvent>()

    init {
        updateUiState()
    }

    fun dateChanged(localDateTime: LocalDateTime) {
        val resultDateTime = if (localDateTime.isAfter(LocalDateTime.now())) LocalDateTime.now() else localDateTime
        uiState.apply { value = value.copy(localDateTime = resultDateTime) }
    }

    // Duration should be between 15 minutes and 23 hours and 45 minutes (23 * 60 + 45 = 1425 minutes)
    fun durationChanged(duration: Duration) {
        uiState.apply {
            value = value.copy(
                duration = Duration.ofMinutes(duration.toMinutes().coerceIn(15, 1425))
            )
        }
    }

    private fun updateUiState() {
        uiState.apply {
            value = value.copy(
                description = traceLocation.description,
                startDateTime = traceLocation.startDate,
                endDateTime = traceLocation.endDate,
                address = traceLocation.address,
            )
        }

        if (traceLocation.startDate != null &&
            traceLocation.startDate != Instant.EPOCH &&
            !traceLocation.isBeforeStartTime(timeStamper.nowUTC)
        ) {
            uiState.apply {
                value = value.copy(localDateTime = traceLocation.startDate.toLocalDateTimeUserTz())
            }
        }

        when {
            traceLocation.endDate != null && traceLocation.endDate != Instant.EPOCH ->
                getNearestFifteen(Duration.between(traceLocation.startDate, traceLocation.endDate).toMinutes())

            traceLocation.defaultCheckInLengthInMinutes != null && traceLocation.defaultCheckInLengthInMinutes > 0 ->
                getNearestFifteen(traceLocation.defaultCheckInLengthInMinutes.toLong())

            else ->
                Duration.ofHours(2)
        }.also { durationChanged(it) }
    }

    private fun getNearestFifteen(number: Long): Duration {
        return Duration.ofMinutes(((number.toFloat() / 15).roundToInt() * 15).toLong())
    }

    data class UiState(
        val description: String = "",
        val address: String = "",
        val startDateTime: Instant? = null,
        val endDateTime: Instant? = null,
        val localDateTime: LocalDateTime = LocalDateTime.now(),
        val duration: Duration = Duration.ofMinutes(15)
    ) {
        fun formattedDateTime(): String {
            val date = localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            val time = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            return "$date $time"
        }

        fun getReadableDuration(resources: Resources): String {
            return duration.toReadableDuration(
                suffix = resources.getString(R.string.tracelocation_organizer_duration_suffix)
            )
        }

        fun getTraceLocationWarnDuration(traceLocation: TraceLocation): TraceLocationWarnDuration {
            val startDate = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
            return TraceLocationWarnDuration(
                traceLocation = traceLocation,
                startDate = startDate,
                endDate = startDate.plus(duration),
            )
        }
    }

    fun goNext() {
        uiState.value.let {
            events.value = TraceLocationWarnDurationEvent.ContinueWithTraceLocationDuration(
                it.getTraceLocationWarnDuration(traceLocation)
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationWarnDurationViewModel> {
        fun create(
            traceLocation: TraceLocation
        ): TraceLocationWarnDurationViewModel
    }
}
