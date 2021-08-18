package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OrganizerSubmissionApiV1 {

    @POST("/version/v1/submission-on-behalf")

    suspend fun submitCheckInsOnBehalf(
        @Header("cwa-authorization") authCode: String,
        @Body requestBody: SubmissionPayload
    ): Response<ResponseBody>
}
