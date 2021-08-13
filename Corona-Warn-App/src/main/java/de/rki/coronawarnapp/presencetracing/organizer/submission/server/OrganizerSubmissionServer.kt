package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
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

        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val checkInPadding = paddingTool.checkInPadding(plausibleParameters, checkInsReport.encryptedCheckIns.size)

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
}
