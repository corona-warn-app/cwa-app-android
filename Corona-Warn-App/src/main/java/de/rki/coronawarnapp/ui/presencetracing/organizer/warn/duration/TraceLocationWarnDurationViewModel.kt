package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
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
import timber.log.Timber

class TraceLocationWarnDurationViewModel @AssistedInject constructor(
    @Assisted private val traceLocationId: Long,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val traceLocationRepository: TraceLocationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private var traceLocation: TraceLocation? = null
    private val uiState = MutableStateFlow(UiState())
    val state: LiveData<UiState> = uiState.asLiveData2()
    val selectedDate: LocalDate get() = uiState.value.localDate
    val selectedTime: LocalTime get() = uiState.value.localTime
    val selectedDuration: Duration get() = uiState.value.duration

    init {
        launch {
            loadTraceLocation()
        }
    }

    fun dateChanged(localDate: LocalDate, localTime: LocalTime) {
        val resultTime = if (localDate == LocalDate.now() &&
            localTime.isAfter(LocalTime.now())
        ) LocalTime.now() else localTime
        uiState.apply { value = value.copy(localDate = localDate, localTime = resultTime) }
    }

    fun durationChanged(duration: Duration) {
        val resultDuration = if (duration == Duration.ZERO) Duration.standardMinutes(15) else duration
        uiState.apply { value = value.copy(duration = resultDuration) }
    }

    private suspend fun loadTraceLocation() {
        try {
            traceLocation = traceLocationRepository.traceLocationForId(traceLocationId).also {
                uiState.apply { value = value.copy(traceLocation = it) }

                if (it.startDate != null &&
                    it.startDate != Instant.EPOCH &&
                    !it.isBeforeStartTime(timeStamper.nowUTC)
                ) {
                    uiState.apply {
                        value = value.copy(
                            localDate = it.startDate.toDateTime().toLocalDate(),
                            localTime = it.startDate.toDateTime().toLocalTime()
                        )
                    }

                    val duration = if (it.endDate != null && it.endDate != Instant.EPOCH) {
                        getNearestFifteen(Duration(it.startDate, it.endDate).standardMinutes)
                    } else if (it.defaultCheckInLengthInMinutes != null && it.defaultCheckInLengthInMinutes > 0) {
                        getNearestFifteen(it.defaultCheckInLengthInMinutes.toLong())
                    } else {
                        Duration.standardHours(2)
                    }

                    uiState.apply {
                        value = value.copy(
                            duration = duration
                        )
                    }
                }
            }
        } catch (exception: Exception) {
            Timber.d(exception, "No location found")
            exception.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun getNearestFifteen(durationMinutes: Long): Duration {
        val minDif = durationMinutes % 15
        val maxDif = 15 - (durationMinutes % 15)
        return Duration.standardMinutes(if (minDif < maxDif) durationMinutes - minDif else durationMinutes + maxDif)
    }

    data class UiState(
        private val traceLocation: TraceLocation? = null,
        val localDate: LocalDate = LocalDate.now(),
        val localTime: LocalTime = LocalTime.now(),
        val duration: Duration = Duration.standardMinutes(15)
    ) {
        val description: String? get() = traceLocation?.description
        val startDateTime: Instant? get() = traceLocation?.startDate
        val endDateTime: Instant? get() = traceLocation?.endDate

        fun formattedDateTime() = "${localDate.toDayFormat()} ${localTime.toShortTimeFormat()}"
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationWarnDurationViewModel> {
        fun create(
            traceLocationId: Long
        ): TraceLocationWarnDurationViewModel
    }
}
