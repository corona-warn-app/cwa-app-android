package de.rki.coronawarnapp.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.PaddingTool.checkInPadding
import de.rki.coronawarnapp.util.PaddingTool.keyPadding

import de.rki.coronawarnapp.util.PaddingTool.requestPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionServer @Inject constructor(
    private val submissionApi: Lazy<SubmissionApiV1>,
    private val appConfigProvider: AppConfigProvider
) {

    private val api: SubmissionApiV1
        get() = submissionApi.get()

    data class SubmissionData(
        val authCode: String,
        val keyList: List<TemporaryExposureKey>,
        val consentToFederation: Boolean,
        val visitedCountries: List<String>,
        val checkIns: List<CheckInOuterClass.CheckIn>
    )

    suspend fun submitPayload(
        data: SubmissionData
    ) = withContext(Dispatchers.IO) {
        Timber.d("submitSubmissionPayload()")

        val authCode = data.authCode
        val keyList = data.keyList
        val checkInList = data.checkIns

        Timber.d(
            "Writing %s Keys and %s CheckIns to the Submission Payload.",
            keyList.size,
            checkInList.size
        )

        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val keyPadding = keyPadding(keyList.size)
        val checkInPadding = checkInPadding(plausibleParameters, checkInList.size)
        val requestPadding = keyPadding + checkInPadding

        val submissionPayload = SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(requestPadding))
            .setConsentToFederation(data.consentToFederation)
            .addAllVisitedCountries(data.visitedCountries)
            .addAllCheckIns(data.checkIns)
            .build()

        api.submitPayload(
            authCode = authCode,
            fake = "0",
            headerPadding = EMPTY_STRING,
            requestBody = submissionPayload
        )
    }

    suspend fun submitFakePayload() = withContext(Dispatchers.IO) {
        Timber.d("submitFakeSubmissionPayload()")

        val plausibleParameters = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .plausibleDeniabilityParameters

        val fakeKeyPadding = keyPadding(keyListSize = 0)
        val fakeCheckInPadding = checkInPadding(plausibleParameters, checkInListSize = 0)
        val requestPadding = fakeKeyPadding + fakeCheckInPadding

        val submissionPayload = SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(requestPadding))
            .build()

        api.submitPayload(
            authCode = EMPTY_STRING,
            fake = "1",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            requestBody = submissionPayload
        )
    }

    companion object {
        private const val EMPTY_STRING = ""
        private const val PADDING_LENGTH_HEADER_SUBMISSION_FAKE = 36
    }
}
