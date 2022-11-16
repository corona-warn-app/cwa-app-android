package de.rki.coronawarnapp.srs.core.server

import com.google.protobuf.ByteString
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class SrsSubmissionServer @Inject constructor(
    srsSubmissionApi: Lazy<SrsSubmissionApi>,
    private val paddingTool: PaddingTool,
    private val appConfigProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider
) {

    private val api = srsSubmissionApi.get()

    suspend fun submit(payload: SrsSubmissionPayload) = withContext(dispatcherProvider.IO) {
        try {
            Timber.tag(TAG).d("submit(payload=%s)", payload)
            submitPayload(payload)
        } catch (e: Exception) {
            throw when (e) {
                is SrsSubmissionException -> e
                is CwaUnknownHostException,
                is NetworkReadTimeoutException,
                is NetworkConnectTimeoutException -> SrsSubmissionException(ErrorCode.SRS_SUB_NO_NETWORK, cause = e)
                // otherwise blame the server
                else -> SrsSubmissionException(ErrorCode.SRS_SUB_SERVER_ERROR, cause = e)
            }
        }
    }

    private suspend fun submitPayload(payload: SrsSubmissionPayload) {
        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val keyList = payload.exposureKeys
        val keyPadding = paddingTool.keyPadding(keyList.size)
        val checkInsReport = payload.checkInsReport
        val checkInPadding = paddingTool.checkInPadding(plausibleParameters, checkInsReport.encryptedCheckIns.size)
        val requestPadding = keyPadding + checkInPadding
        Timber.tag(TAG).d(
            "keyPadding=%s\ncheckInPadding=%s\nrequestPadding=%s",
            keyPadding,
            checkInPadding,
            requestPadding
        )

        @Suppress("DEPRECATION")
        val submissionPayload = SubmissionPayloadOuterClass.SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(requestPadding))
            .setConsentToFederation(false)
            .addAllVisitedCountries(payload.visitedCountries)
            .addAllCheckIns(checkInsReport.unencryptedCheckIns)
            .addAllCheckInProtectedReports(checkInsReport.encryptedCheckIns)
            .setSubmissionType(payload.submissionType)
            .build()

        val bodyResponse = api.submitPayload(payload.srsOtp.uuid.toString(), submissionPayload)
        if (bodyResponse.isSuccessful) {
            Timber.i("SRS submission is successful!")
            return
        }

        throw when (bodyResponse.code()) {
            400 -> SrsSubmissionException(ErrorCode.SRS_SUB_400)
            403 -> SrsSubmissionException(ErrorCode.SRS_SUB_403)
            in 400..499 -> SrsSubmissionException(ErrorCode.SRS_SUB_CLIENT_ERROR)
            // error code in 500..599
            else -> SrsSubmissionException(ErrorCode.SRS_SUB_SERVER_ERROR)
        }
    }

    companion object {
        val TAG = tag<SrsSubmissionServer>()
    }
}
