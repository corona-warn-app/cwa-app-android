package de.rki.coronawarnapp.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.PaddingTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionServer @Inject constructor(
    private val submissionApi: Lazy<SubmissionApiV1>,
    private val appConfigProvider: AppConfigProvider,
    private val paddingTool: PaddingTool,
) {

    private val api: SubmissionApiV1
        get() = submissionApi.get()

    data class SubmissionData(
        val authCode: String,
        val keyList: List<TemporaryExposureKey>,
        val consentToFederation: Boolean,
        val visitedCountries: List<String>,
        val checkIns: List<CheckInOuterClass.CheckIn>,
        val submissionType: SubmissionPayload.SubmissionType
    )

    suspend fun submitPayload(
        data: SubmissionData
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("submitPayload(data=%s)", data)

        val authCode = data.authCode
        val keyList = data.keyList
        val checkInList = data.checkIns

        Timber.tag(TAG).d(
            "Writing %s Keys and %s CheckIns to the Submission Payload.",
            keyList.size,
            checkInList.size
        )

        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val keyPadding = paddingTool.keyPadding(keyList.size)
        val checkInPadding = paddingTool.checkInPadding(plausibleParameters, checkInList.size)
        val requestPadding = keyPadding + checkInPadding
        Timber.tag(TAG).d(
            "keyPadding=%s\ncheckInPadding=%s\nrequestPadding=%s",
            keyPadding,
            checkInPadding,
            requestPadding
        )

        val submissionPayload = SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(requestPadding))
            .setConsentToFederation(data.consentToFederation)
            .addAllVisitedCountries(data.visitedCountries)
            .addAllCheckIns(data.checkIns)
            .setSubmissionType(data.submissionType)
            .build()

        api.submitPayload(
            authCode = authCode,
            fake = "0",
            headerPadding = EMPTY_STRING,
            requestBody = submissionPayload
        )
    }

    suspend fun submitFakePayload() = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("submitFakePayload()")

        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val fakeKeyPadding = paddingTool.keyPadding(keyListSize = 0)
        val fakeCheckInPadding = paddingTool.checkInPadding(plausibleParameters, checkInListSize = 0)
        val fakeRequestPadding = fakeKeyPadding + fakeCheckInPadding

        Timber.tag(TAG).v(
            "fakeKeyPadding=%s\nfakeCheckInPadding=%s\nfakeRequestPadding=%s",
            fakeKeyPadding,
            fakeCheckInPadding,
            fakeRequestPadding
        )

        val submissionPayload = SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(fakeRequestPadding))
            .build()

        api.submitPayload(
            authCode = EMPTY_STRING,
            fake = "1",
            headerPadding = paddingTool.requestPadding(PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            requestBody = submissionPayload
        )
    }

    companion object {
        private const val EMPTY_STRING = ""
        private const val PADDING_LENGTH_HEADER_SUBMISSION_FAKE = 36
        private const val TAG = "SubmissionServer"
    }
}
