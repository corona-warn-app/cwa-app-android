package de.rki.coronawarnapp.eventregistration.events.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import de.rki.coronawarnapp.eventregistration.events.TRACE_LOCATION_VERSION
import de.rki.coronawarnapp.eventregistration.events.TraceLocationUserInput
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationServer @Inject constructor(
    private val api: Lazy<CreateTraceLocationApiV1>
) {

    suspend fun retrieveSignedTraceLocation(
        traceLocationUserInput: TraceLocationUserInput
    ): TraceLocationOuterClass.SignedTraceLocation {

        val traceLocationProto = traceLocationUserInput.toTraceLocationProtoBuf()
        val response = api.get().createTraceLocation(traceLocationProto)

        if (!response.isSuccessful) throw HttpException(response)
        if (response.body() == null) {
            throw IllegalStateException("Response is successful, but body is empty.")
        }

        val signedTraceLocation = response.body()!!

        Timber.d("Successfully received SignedTraceLocation: $signedTraceLocation")
        return signedTraceLocation
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun TraceLocationUserInput.toTraceLocationProtoBuf(): TraceLocationOuterClass.TraceLocation {
    return TraceLocationOuterClass.TraceLocation.newBuilder()
        .setVersion(TRACE_LOCATION_VERSION)
        .setType(type)
        .setDescription(description)
        .setAddress(address)
        .setStartTimestamp(startDate?.seconds ?: 0)
        .setEndTimestamp(endDate?.seconds ?: 0)
        .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes)
        .build()
}
