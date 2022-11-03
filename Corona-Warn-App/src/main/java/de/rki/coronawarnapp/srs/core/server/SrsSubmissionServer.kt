package de.rki.coronawarnapp.srs.core.server

import com.google.protobuf.ByteString
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
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
        Timber.tag(TAG).d("submit()")
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

        api.submitPayload(payload.srsOtp.uuid, submissionPayload)
    }

    companion object {
        val TAG = tag<SrsSubmissionServer>()
    }
}
