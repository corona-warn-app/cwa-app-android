package de.rki.coronawarnapp.srs.core.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.inject.Inject
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationResponse
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime

@Reusable
class SrsAuthorizationServer @Inject constructor(
    srsAuthorizationApi: Lazy<SrsAuthorizationApi>,
    @BaseJackson private val mapper: ObjectMapper,
    private val dispatcherProvider: DispatcherProvider,
    private val srsDevSettings: SrsDevSettings,
) {
    private val api = srsAuthorizationApi.get()

    suspend fun authorize(request: SrsAuthorizationRequest): Instant =
        withContext(dispatcherProvider.IO) {
            try {
                authoriseRequest(request)
            } catch (e: Exception) {
                throw when (e) {
                    is SrsSubmissionException -> e
                    is CwaUnknownHostException,
                    is NetworkReadTimeoutException,
                    is NetworkConnectTimeoutException -> SrsSubmissionException(ErrorCode.SRS_OTP_NO_NETWORK, cause = e)
                    // otherwise blame the server
                    else -> SrsSubmissionException(ErrorCode.SRS_OTP_SERVER_ERROR, cause = e)
                }
            }
        }

    private suspend fun authoriseRequest(request: SrsAuthorizationRequest): Instant {
        Timber.tag(TAG).d("authorize(request=%s)", request)
        val srsOtpRequest = SRSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(
                SRSOneTimePasswordRequestAndroid.SRSOneTimePassword
                    .newBuilder()
                    .setOtp(request.srsOtp.uuid.toString())
                    .setAndroidId(request.androidId)
                    .build()
            )
            .setAuthentication(
                PpacAndroid.PPACAndroid.newBuilder()
                    .setSafetyNetJws(request.safetyNetJws)
                    .setSalt(request.salt)
                    .build()
            )
            .build()

        val headers = mutableMapOf("Content-Type" to "application/x-protobuf").apply {
            if (srsDevSettings.forceAndroidIdAcceptance()) {
                Timber.tag(TAG).d("forceAndroidIdAcceptance is enabled")
                put("cwa-ppac-android-accept-android-id", "1")
            }
        }
        val bodyResponse = api.authenticate(headers, srsOtpRequest)
        val response = bodyResponse.body()?.charStream()?.use { mapper.readValue<SrsAuthorizationResponse>(it) }
        return when {
            response?.errorCode != null -> throw SrsSubmissionException(ErrorCode.fromAuthErrorCode(response.errorCode))
            response?.expirationDate != null -> OffsetDateTime.parse(response.expirationDate).toInstant()
            else -> throw when (bodyResponse.code()) {
                400 -> SrsSubmissionException(ErrorCode.SRS_OTP_400)
                401 -> SrsSubmissionException(ErrorCode.SRS_OTP_401)
                403 -> SrsSubmissionException(ErrorCode.SRS_OTP_403)
                in 400..499 -> SrsSubmissionException(ErrorCode.SRS_OTP_CLIENT_ERROR)
                // error code in 500..599
                else -> SrsSubmissionException(ErrorCode.SRS_OTP_SERVER_ERROR)
            }
        }
    }

    companion object {
        val TAG = tag<SrsAuthorizationServer>()
    }
}
