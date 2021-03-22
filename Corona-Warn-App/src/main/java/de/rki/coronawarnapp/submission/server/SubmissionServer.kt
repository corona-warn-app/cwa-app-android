package de.rki.coronawarnapp.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
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

    suspend fun submitKeysToServer(
        data: SubmissionData
    ) = withContext(Dispatchers.IO) {
        Timber.d("submitKeysToServer()")
        val authCode = data.authCode
        val keyList = data.keyList
        Timber.d("Writing ${keyList.size} Keys to the Submission Payload.")

        val fakeKeyPadding = keyPadding(keyList.size)
        val submissionPayload = SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .setConsentToFederation(data.consentToFederation)
            .addAllVisitedCountries(data.visitedCountries)
            .addAllCheckIns(data.checkIns)
            .build()

        api.submitKeys(
            authCode = authCode,
            fake = "0",
            headerPadding = EMPTY_HEADER,
            requestBody = submissionPayload
        )
    }

    suspend fun submitKeysToServerFake() = withContext(Dispatchers.IO) {
        Timber.d("submitKeysToServerFake()")

        val fakeKeyPadding = keyPadding(keyListSize = 0)
        val submissionPayload = SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .build()

        api.submitKeys(
            authCode = EMPTY_HEADER,
            fake = "1",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            requestBody = submissionPayload
        )
    }

    companion object {
        const val EMPTY_HEADER = ""
        const val PADDING_LENGTH_HEADER_SUBMISSION_FAKE = 36
    }
}
