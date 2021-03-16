package de.rki.coronawarnapp.test.eventregistration.ui.createevent

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.events.DefaultTraceLocation
import de.rki.coronawarnapp.eventregistration.events.TRACE_LOCATION_VERSION
import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.UUID

class CreateEventTestViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CreateEventTestViewModel>

    val result = MutableLiveData<Result>()

    fun createEvent(
        type: String,
        description: String,
        address: String,
        start: String,
        end: String,
        defaultCheckInLengthInMinutes: String,
        sendToServer: Boolean = false
    ) {
        try {
            val startDate =
                if (start.isBlank()) null else DateTime.parse(start, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))
            val endDate =
                if (end.isBlank()) null else DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))

            // Backend needs UNIX timestamp in Seconds, not milliseconds
            val startTimeStampSeconds = startDate?.toInstant()?.seconds ?: 0
            val endTimeStampSeconds = endDate?.toInstant()?.seconds ?: 0

            val traceLocationType =
                if (type == "Event") LOCATION_TYPE_TEMPORARY_OTHER else LOCATION_TYPE_TEMPORARY_OTHER

            if (sendToServer) {
                TraceLocationOuterClass.TraceLocation.newBuilder()
                    .setVersion(TRACE_LOCATION_VERSION)
                    .setType(traceLocationType)
                    .setDescription(description)
                    .setAddress(address)
                    .setStartTimestamp(startTimeStampSeconds)
                    .setEndTimestamp(endTimeStampSeconds)
                    .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes.toInt())
                    .build()
            }

            // TODO: Replace with enum values once https://github.com/corona-warn-app/cwa-app-android/pull/2624 is merged
            val traceLocationTypeLegacy =
                if (type == "Event") TraceLocation.Type.PERMANENT_OTHER else TraceLocation.Type.TEMPORARY_OTHER

            val traceLocation = DefaultTraceLocation(
                UUID.randomUUID().toString(), // will be provided by the server when the endpoint is ready
                traceLocationTypeLegacy,
                description,
                address,
                startDate?.toInstant(),
                endDate?.toInstant(),
                defaultCheckInLengthInMinutes.toInt(),
                "ServerSignature"
            )

            traceLocationRepository.addTraceLocation(traceLocation)
            result.postValue(Result.Success(traceLocation))
        } catch (exception: Exception) {
            Timber.d("Something went wrong when trying to create an event: $exception")
            result.postValue(Result.Error)
        }
    }

    sealed class Result {
        object Error : Result()
        data class Success(val eventEntity: TraceLocation) : Result()
    }
}
