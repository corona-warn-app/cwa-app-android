package de.rki.coronawarnapp.eventregistration.events.server

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CreateTraceLocationApiV1 {

    @POST("/version/v1/trace-location")
    suspend fun createTraceLocation(
        @Body requestBody: TraceLocationOuterClass.TraceLocation
    ): Response<TraceLocationOuterClass.SignedTraceLocation>
}
