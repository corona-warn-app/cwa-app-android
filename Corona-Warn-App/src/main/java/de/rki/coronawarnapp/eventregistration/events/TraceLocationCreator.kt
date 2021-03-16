package de.rki.coronawarnapp.eventregistration.events

import dagger.Lazy
import de.rki.coronawarnapp.eventregistration.events.server.TraceLocationServer
import javax.inject.Inject
import javax.inject.Singleton

const val TRACE_LOCATION_VERSION = 1

@Singleton
class TraceLocationCreator @Inject constructor(
    private val api: Lazy<TraceLocationServer>
) {

    suspend fun createTraceLocation(traceLocationUserInput: TraceLocationUserInput): TraceLocation {

        // TODO: Input validation

        val traceLocation = api.get().createTraceLocation(traceLocationUserInput)

        // TODO: Signature verification

        return traceLocation
    }
}
