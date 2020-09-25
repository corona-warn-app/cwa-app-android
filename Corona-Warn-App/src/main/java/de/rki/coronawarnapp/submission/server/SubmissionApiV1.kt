package de.rki.coronawarnapp.submission.server

import KeyExportFormat
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SubmissionApiV1 {

    @POST("version/v1/diagnosis-keys")
    suspend fun submitKeys(
        @Header("cwa-authorization") authCode: String?,
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: KeyExportFormat.SubmissionPayload
    ): ResponseBody
}
