package de.rki.coronawarnapp.test.eventregistration.ui.createevent

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import okio.ByteString.Companion.toByteString
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
        defaultCheckInLengthInMinutes: String
    ) {
        try {
            val startDate =
                if (start.isBlank()) null else DateTime.parse(start, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))
            val endDate =
                if (end.isBlank()) null else DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))

            /* TODO: wait for new protobuf messages 'TraceLocation' and perform network request to get
                'SignedTraceLocation' */

            // Backend needs UNIX timestamp in Seconds, not milliseconds
            val startTimeStampSeconds = startDate?.toInstant()?.seconds ?: 0
            val endTimeStampSeconds = endDate?.toInstant()?.seconds ?: 0

            val traceLocationType =
                if (type == "Event") TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
                else TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER

            val traceLocation = TraceLocation(
                UUID.randomUUID().toString(), // will be provided by the server when the endpoint is ready
                traceLocationType,
                description,
                address,
                startDate?.toInstant(),
                endDate?.toInstant(),
                defaultCheckInLengthInMinutes.toInt(),
                "ServerSignature".toByteArray().toByteString()
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
