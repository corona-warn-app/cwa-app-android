package de.rki.coronawarnapp.test.eventregistration.ui.createevent

import androidx.lifecycle.MutableLiveData
import com.google.protobuf.ByteString
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import de.rki.coronawarnapp.eventregistration.events.toHostedEvent
import de.rki.coronawarnapp.eventregistration.storage.repo.HostedEventRepository
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.UUID

class CreateEventTestViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val hostedEventRepository: HostedEventRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CreateEventTestViewModel>

    val result = MutableLiveData<Result>()

    fun createEvent(
        description: String,
        location: String,
        start: String,
        end: String,
        defaultCheckInLengthInMinutes: String
    ) {
        launch {
            try {
                val startDate =
                    if (start.isBlank()) null else DateTime.parse(start, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))
                val endDate =
                    if (end.isBlank()) null else DateTime.parse(end, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))

                val startTimeStamp = startDate?.toInstant()?.millis?.toInt() ?: 0
                val endTimeStamp = endDate?.toInstant()?.millis?.toInt() ?: 0

                // details yet tbd, but we basically sent our event entity to the backend ...
                val event = EventOuterClass.Event.newBuilder()
                    .setDescription(description)
                    // .setLocation(location) // will probably added in a future protobuf
                    .setStart(startTimeStamp)
                    .setEnd(endTimeStamp)
                    .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes.toInt())

                // and the server responds with an event object with additional information
                val serverEvent = event
                    .setGuid(ByteString.copyFrom(UUID.randomUUID().toString().toByteArray())) // Server creates GUID
                    .build()

                val signedEvent = SignedEventOuterClass.SignedEvent.newBuilder()
                    .setEvent(serverEvent)
                    .setSignature(ByteString.copyFrom("ServerSignature".toByteArray()))
                    .build()

                val hostedEvent = signedEvent.toHostedEvent()

                hostedEventRepository.addHostedEvent(hostedEvent)
                result.postValue(Result.Success(hostedEvent))
            } catch (exception: Exception) {
                Timber.d("Something went wrong when trying to create an event: $exception")
                result.postValue(Result.Error)
            }
        }
    }

    sealed class Result {
        object Error : Result()
        data class Success(val eventEntity: HostedEvent) : Result()
    }
}
