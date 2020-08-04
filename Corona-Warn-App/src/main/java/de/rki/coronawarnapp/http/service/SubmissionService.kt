package de.rki.coronawarnapp.http.service

import KeyExportFormat
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface SubmissionService {
    @POST
    suspend fun submitKeys(
        @Url url: String,
        @Header("cwa-authorization") authCode: String?,
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: KeyExportFormat.SubmissionPayload
    ): ResponseBody
}
