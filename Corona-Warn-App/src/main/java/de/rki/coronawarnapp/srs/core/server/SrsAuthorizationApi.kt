package de.rki.coronawarnapp.srs.core.server

import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface SrsAuthorizationApi {

    @POST("version/v1/android/srs")
    suspend fun authenticate(
        @HeaderMap headers: Map<String, String>,
        @Body requestBody: SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid
    ): Response<ResponseBody>
}
