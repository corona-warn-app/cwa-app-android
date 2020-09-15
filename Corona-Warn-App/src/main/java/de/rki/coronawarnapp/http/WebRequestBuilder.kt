/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.http

import KeyExportFormat
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.http.requests.RegistrationRequest
import de.rki.coronawarnapp.http.requests.RegistrationTokenRequest
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.security.HashHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.max

class WebRequestBuilder(
    private val verificationService: VerificationService,
    private val submissionService: SubmissionService
) {
    companion object {
        private val TAG: String? = WebRequestBuilder::class.simpleName

        @Volatile
        private var instance: WebRequestBuilder? = null

        fun getInstance(): WebRequestBuilder {
            return instance ?: synchronized(this) {
                instance ?: buildWebRequestBuilder().also { instance = it }
            }
        }

        private fun buildWebRequestBuilder(): WebRequestBuilder {
            val serviceFactory = AppInjector.component.serviceFactory
            return WebRequestBuilder(
                serviceFactory.verificationService(),
                serviceFactory.submissionService()
            )
        }
    }

    suspend fun asyncGetRegistrationToken(
        key: String,
        keyType: KeyType
    ): String = withContext(Dispatchers.IO) {
        val keyStr = if (keyType == KeyType.GUID) {
            HashHelper.hash256(key)
        } else {
            key
        }

        val paddingLength = when (keyType) {
            KeyType.GUID -> SubmissionConstants.PADDING_LENGTH_BODY_REGISTRATION_TOKEN_GUID
            KeyType.TELETAN -> SubmissionConstants.PADDING_LENGTH_BODY_REGISTRATION_TOKEN_TELETAN
        }

        verificationService.getRegistrationToken(
            SubmissionConstants.REGISTRATION_TOKEN_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_REGISTRATION_TOKEN),
            RegistrationTokenRequest(keyType.name, keyStr, requestPadding(paddingLength))
        ).registrationToken
    }

    suspend fun asyncGetTestResult(
        registrationToken: String
    ): Int = withContext(Dispatchers.IO) {
        verificationService.getTestResult(
            SubmissionConstants.TEST_RESULT_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TEST_RESULT),
            RegistrationRequest(
                registrationToken,
                requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TEST_RESULT)
            )
        ).testResult
    }

    suspend fun asyncGetTan(
        registrationToken: String
    ): String = withContext(Dispatchers.IO) {
        verificationService.getTAN(
            SubmissionConstants.TAN_REQUEST_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TAN),
            TanRequestBody(
                registrationToken,
                requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TAN)
            )
        ).tan
    }

    suspend fun asyncFakeVerification() = withContext(Dispatchers.IO) {
        verificationService.getTAN(
            SubmissionConstants.TAN_REQUEST_URL,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TAN),
            TanRequestBody(
                registrationToken = SubmissionConstants.DUMMY_REGISTRATION_TOKEN,
                requestPadding = requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TAN_FAKE)
            )
        )
    }

    suspend fun asyncSubmitKeysToServer(
        authCode: String,
        visitedCountries: List<String>,
        consentToFederation: Boolean,
        keyList: List<KeyExportFormat.TemporaryExposureKey>
    ) = withContext(Dispatchers.IO) {
        Timber.d("Writing ${keyList.size} Keys to the Submission Payload.")

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount =
            max(SubmissionConstants.minKeyCountForSubmission + randomAdditions - keyList.size, 0)
        val fakeKeyPadding = requestPadding(SubmissionConstants.fakeKeySize * fakeKeyCount)

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .addAllVisitedCountries(visitedCountries)
            .setConsentToFederation(consentToFederation)
            .build()
        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            authCode,
            "0",
            SubmissionConstants.EMPTY_HEADER,
            submissionPayload
        )
        return@withContext
    }

    suspend fun asyncFakeSubmission() = withContext(Dispatchers.IO) {

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount = SubmissionConstants.minKeyCountForSubmission + randomAdditions

        val fakeKeyPadding =
            requestPadding(SubmissionConstants.fakeKeySize * fakeKeyCount)

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .setPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .build()

        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            SubmissionConstants.EMPTY_HEADER,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            submissionPayload
        )
    }

    private fun requestPadding(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
