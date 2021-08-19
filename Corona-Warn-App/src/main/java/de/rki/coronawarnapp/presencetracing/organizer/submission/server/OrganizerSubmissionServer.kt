package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionException
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class OrganizerSubmissionServer @Inject constructor(
    private val paddingTool: PaddingTool,
    private val appConfigProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider,
    private val organizerSubmissionApiV1Lazy: Lazy<OrganizerSubmissionApiV1>
) {

    private val organizerSubmissionApiV1: OrganizerSubmissionApiV1
        get() = organizerSubmissionApiV1Lazy.get()

    suspend fun submit(
        uploadTAN: String,
        checkInsReport: CheckInsReport
    ): Unit = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("submit(uploadTAN=%s, checkInReport=%s)", uploadTAN, checkInsReport)
        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val checkInPadding = paddingTool.checkInPadding(
            plausibleParameters = plausibleParameters,
            checkInListSize = checkInsReport.encryptedCheckIns.size
        )

        val payload = SubmissionPayload.newBuilder()
            .addAllKeys(emptyList())
            .setRequestPadding(ByteString.copyFromUtf8(checkInPadding))
            .setConsentToFederation(false)
            .addAllVisitedCountries(emptyList())
            .addAllCheckIns(checkInsReport.unencryptedCheckIns)
            .addAllCheckInProtectedReports(checkInsReport.encryptedCheckIns)
            .setSubmissionType(SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
            .build()

        try {
            organizerSubmissionApiV1.submitCheckInsOnBehalf(uploadTAN, payload)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Submission of %s failed", checkInsReport)
            throw when (e) {
                is CwaUnknownHostException, is NetworkConnectTimeoutException -> OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_NO_NETWORK
                is CwaClientError -> OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_CLIENT_ERROR // HTTP status code 4XX
                is CwaServerError -> OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_SERVER_ERROR // HTTP status code 5XX
                else -> {
                    Timber.tag(TAG).d(
                        "Mapping unknown exception %s to SUBMISSION_OB_SERVER_ERROR",
                        e::class.java.simpleName
                    )
                    OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_SERVER_ERROR
                }
            }.let { OrganizerSubmissionException(it, e) }
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
