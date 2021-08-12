package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.coronatest.server.VerificationApiV1
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OrganizerSubmissionApi {
    @Headers("Content-Type: application/x-protobuf")
    @POST("/version/v1/submission-on-behalf")
    suspend fun submit(
        @Header("cwa-authorization") tan: String,
        @Body body: SubmissionPayloadOuterClass.SubmissionPayload
    ): VerificationApiV1.TestResultResponse
}
