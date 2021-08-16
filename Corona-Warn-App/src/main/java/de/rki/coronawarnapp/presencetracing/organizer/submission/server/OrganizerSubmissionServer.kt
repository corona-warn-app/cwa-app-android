package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OrganizerSubmissionServer @Inject constructor(
    private val paddingTool: PaddingTool,
    private val appConfigProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun submit(
        uploadTAN: String,
        checkInsReport: CheckInsReport
    ) = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("submit(uploadTAN=%s, checkInReport=%s)", uploadTAN, checkInsReport)
        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val checkInPadding = paddingTool.checkInPadding(
            plausibleParameters = plausibleParameters,
            checkInListSize = checkInsReport.encryptedCheckIns.size
        )

        val submissionPayload = SubmissionPayload.newBuilder()
            .addAllKeys(emptyList())
            .setRequestPadding(ByteString.copyFromUtf8(checkInPadding))
            .setConsentToFederation(false)
            .addAllVisitedCountries(emptyList())
            .addAllCheckIns(checkInsReport.unencryptedCheckIns)
            .addAllCheckInProtectedReports(checkInsReport.encryptedCheckIns)
            .setSubmissionType(SubmissionPayload.SubmissionType.SUBMISSION_TYPE_PCR_TEST) // TODO TBD
            .build()

        // TODO submit
    }

    suspend fun submitFakePayload() = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("submitFakePayload()")
        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val fakeCheckInPadding = paddingTool.checkInPadding(plausibleParameters, checkInListSize = 0)

        Timber.tag(TAG).v("fakeCheckInPadding=%s", fakeCheckInPadding)

        val submissionPayload = SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(fakeCheckInPadding))
            .build()

        // TODO submit fake payload
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
