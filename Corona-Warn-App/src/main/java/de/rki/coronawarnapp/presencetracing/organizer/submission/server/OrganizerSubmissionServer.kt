package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.tag
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

        @Suppress("DEPRECATION")
        val payload = SubmissionPayload.newBuilder()
            .addAllKeys(emptyList())
            .setRequestPadding(ByteString.copyFromUtf8(checkInPadding))
            .setConsentToFederation(false)
            .addAllVisitedCountries(emptyList())
            .addAllCheckIns(checkInsReport.unencryptedCheckIns)
            .addAllCheckInProtectedReports(checkInsReport.encryptedCheckIns)
            .setSubmissionType(SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
            .build()

        organizerSubmissionApiV1.submitCheckInsOnBehalf(uploadTAN, payload)
    }

    companion object {
        private val TAG = tag<OrganizerSubmissionServer>()
    }
}
