package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import com.fasterxml.jackson.annotation.JsonProperty
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LogUploadApiV1 {

    @Multipart
    @POST("/api/logs")
    suspend fun uploadLog(
        @Header("cwa-otp") otp: String,
        @Part logZip: MultipartBody.Part
    ): UploadResponse

    data class UploadResponse(
        @JsonProperty("id") val id: String,
        @JsonProperty("hash") val hash: String?,
        @JsonProperty("errorCode") val errorCode: String?
    )
}
