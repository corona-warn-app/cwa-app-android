package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LogUploadApi {

    @Multipart
    @POST("/api/logs")
    suspend fun uploadLog(
        @Header("cwa-otp") otp: String,
        @Part logZip: MultipartBody.Part
    ): UploadResponse

    data class UploadResponse(
        @SerializedName("id") val id: String,
        @SerializedName("hash") val hash: String,
        @SerializedName("errorCode") val errorCode: String?
    )
}
