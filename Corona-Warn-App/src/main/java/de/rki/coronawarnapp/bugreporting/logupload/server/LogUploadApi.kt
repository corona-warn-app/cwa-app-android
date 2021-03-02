package de.rki.coronawarnapp.bugreporting.logupload.server

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LogUploadApi {

    @Multipart
    @POST("/version/v1/android/logs")
    suspend fun uploadLog(
        @Header("cwa-otp") otp: String,
        @Part("file") logZip: MultipartBody.Part
    ): Response<UploadResponse>

    data class UploadResponse(
        @SerializedName("errorCode") val errorCode: String?
    )
}
