package de.rki.coronawarnapp.srs.core.server

import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SrsSubmissionApi {
    @Throws(CwaWebException::class)
    @POST("version/v1/diagnosis-keys")
    @Headers("Content-Type: application/x-protobuf")
    suspend fun submitPayload(
        @Header("cwa-otp") otp: String,
        @Body requestBody: SubmissionPayloadOuterClass.SubmissionPayload
    ): Response<ResponseBody>
}
