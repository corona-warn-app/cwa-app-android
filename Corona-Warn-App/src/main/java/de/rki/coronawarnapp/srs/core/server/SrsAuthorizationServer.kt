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
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationResponse
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
    private val dispatcherProvider: DispatcherProvider,
    @BaseJackson private val mapper: ObjectMapper
) {
    private val api = srsAuthorizationApi.get()

    suspend fun authorize(request: SrsAuthorizationRequest): Instant =
        withContext(dispatcherProvider.IO) {
            try {
                Timber.tag(TAG).d("authorize(request=%s)", request)
                val srsOtpRequest = SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid.newBuilder()
                    .setPayload(
                        SrsOtp.SRSOneTimePassword.newBuilder().setOtp(request.srsOtp.uuid.toString()).build()
                    )
                    .setAuthentication(
                        PpacAndroid.PPACAndroid.newBuilder()
                            .setAndroidId(request.androidId)
                            .setSafetyNetJws(request.safetyNetJws)
                            .setSalt(request.salt)
                            .build()
                    )
                    .build()
                val bodyResponse = api.authenticate(srsOtpRequest)
                val response = bodyResponse.body()?.charStream()?.use { mapper.readValue<SrsAuthorizationResponse>(it) }
                when {
                    response?.expirationDate != null -> OffsetDateTime.parse(response.expirationDate).toInstant()
                    response?.errorCode != null -> throw SrsSubmissionException(ErrorCode.from(response.errorCode))
                    else -> throw when (bodyResponse.code()) {
                        400 -> SrsSubmissionException(ErrorCode.SRS_OTP_400)
                        401 -> SrsSubmissionException(ErrorCode.SRS_OTP_401)
                        403 -> SrsSubmissionException(ErrorCode.SRS_OTP_403)
                        in 400..499 -> SrsSubmissionException(ErrorCode.SRS_OTP_CLIENT_ERROR)
                        // error code in 500..599
                        else -> SrsSubmissionException(ErrorCode.SRS_OTP_SERVER_ERROR)
                    }
                }
            } catch (e: Exception) {
                throw when (e) {
                    is SrsSubmissionException -> e
                    is CwaUnknownHostException,
                    is NetworkReadTimeoutException,
                    is NetworkConnectTimeoutException -> SrsSubmissionException(ErrorCode.SRS_OTO_NO_NETWORK)

                    else -> SrsSubmissionException(ErrorCode.SRS_OTP_SERVER_ERROR, cause = e)
                }
            }
        }

    companion object {
        val TAG = tag<SrsAuthorizationServer>()
    }
}
