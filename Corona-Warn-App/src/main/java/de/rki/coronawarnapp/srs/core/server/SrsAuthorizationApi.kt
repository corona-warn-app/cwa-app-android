package de.rki.coronawarnapp.srs.core.server

import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SrsAuthorizationApi {

    @POST("version/v1/android/srs")
    @Headers("Content-Type: application/x-protobuf")
    suspend fun authenticate(
        @Body requestBody: SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid
    ): Response<ResponseBody>
}
