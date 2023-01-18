package de.rki.coronawarnapp.srs.core.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.protobuf.ByteString
import javax.inject.Inject
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfig
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationFakeRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationResponse
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime

@Reusable
class SrsAuthorizationServer @Inject constructor(
    srsAuthorizationApi: Lazy<SrsAuthorizationApi>,
    @BaseJackson private val mapper: ObjectMapper,
    private val dispatcherProvider: DispatcherProvider,
    private val srsDevSettings: SrsDevSettings,
    private val appConfigProvider: AppConfigProvider,
    private val paddingTool: PaddingTool,
) {
    private val api = srsAuthorizationApi.get()

    suspend fun authorize(request: SrsAuthorizationRequest): Instant =
        withContext(dispatcherProvider.IO) {
            try {
                authorizeRequest(request)
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

    suspend fun fakeAuthorize(request: SrsAuthorizationFakeRequest): Result<Response<ResponseBody>> = runCatching {
        Timber.tag(TAG).d("fakeAuthorize()")
        val selfReportSubmission = appConfigProvider.currentConfig.first().selfReportSubmission
        val min = selfReportSubmission.common.plausibleDeniabilityParameters.minRequestPaddingBytes
        val max = selfReportSubmission.common.plausibleDeniabilityParameters.maxRequestPaddingBytes
        val authPadding = ByteString.copyFrom(paddingTool.srsAuthPadding(min, max))

        Timber.tag(TAG).d("authPadding=%s, min=%s, max=%s", authPadding, min, max)
        val srsOtpRequest = SRSOneTimePasswordRequestAndroid.newBuilder()
            .setAuthentication(
                PpacAndroid.PPACAndroid.newBuilder()
                    .setSafetyNetJws(request.safetyNetJws)
                    .setSalt(request.salt)
                    .build()
            )
            .setRequestPadding(authPadding)
            .build()

        val headers = mapOf(
            "Content-Type" to "application/x-protobuf",
            "cwa-fake" to "1",
        )
        api.authenticate(headers, srsOtpRequest)
    }.onFailure {
        Timber.tag(TAG).d("fakeAuthorize() failed -> %s", it.localizedMessage)
    }

    private suspend fun authorizeRequest(request: SrsAuthorizationRequest): Instant {
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

        val headers = mutableMapOf(
            "Content-Type" to "application/x-protobuf",
            "cwa-fake" to "0",
        ).apply {
            if (srsDevSettings.forceAndroidIdAcceptance()) {
                Timber.tag(TAG).d("forceAndroidIdAcceptance is enabled")
                put("cwa-ppac-android-accept-android-id", "1")
            }
        }
        val bodyResponse = api.authenticate(headers, srsOtpRequest)
        val response = bodyResponse.body()?.charStream()?.use { mapper.readValue<SrsAuthorizationResponse>(it) }
        val serverErrorCode = getServerErrorCode(bodyResponse)
        return when {
            serverErrorCode != null -> {
                val errorCode = ErrorCode.fromAuthErrorCode(serverErrorCode)
                throw SrsSubmissionException(
                    errorCode = errorCode,
                    errorArgs = errorCode.errorArgs(appConfigProvider.currentConfig.first().selfReportSubmission)
                )
            }

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

    private fun getServerErrorCode(bodyResponse: Response<ResponseBody>): String? = runCatching {
        bodyResponse.errorBody()?.charStream()?.use {
            mapper.readValue<SrsAuthorizationResponse>(it)
        }?.errorCode
    }.onFailure {
        Timber.d(it, "getServerErrorCode()")
    }.getOrNull()

    companion object {
        val TAG = tag<SrsAuthorizationServer>()
    }
}

fun ErrorCode.errorArgs(selfReportSubmission: SelfReportSubmissionConfig): Array<Any> = when (this) {
    ErrorCode.DEVICE_QUOTA_EXCEEDED,
    ErrorCode.SUBMISSION_TOO_EARLY -> arrayOf(
        selfReportSubmission
            .common
            .timeBetweenSubmissionsInDays
            .toDays()
    )

    ErrorCode.TIME_SINCE_ONBOARDING_UNVERIFIED,
    ErrorCode.MIN_TIME_SINCE_ONBOARDING -> {
        val hours = selfReportSubmission
            .common
            .timeSinceOnboardingInHours
            .toHours()
        arrayOf(hours, hours)
    }

    else -> emptyArray()
}
