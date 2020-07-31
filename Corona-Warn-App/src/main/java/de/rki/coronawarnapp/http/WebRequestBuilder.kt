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
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.http.requests.RegistrationRequest
import de.rki.coronawarnapp.http.requests.RegistrationTokenRequest
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.HashHelper
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.UUID

class WebRequestBuilder(
    private val distributionService: DistributionService,
    private val verificationService: VerificationService,
    private val submissionService: SubmissionService,
    private val verificationKeys: VerificationKeys
) {
    companion object {
        private val TAG: String? = WebRequestBuilder::class.simpleName
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

        @Volatile
        private var instance: WebRequestBuilder? = null

        fun getInstance(): WebRequestBuilder {
            return instance ?: synchronized(this) {
                instance ?: buildWebRequestBuilder().also { instance = it }
            }
        }

        private fun buildWebRequestBuilder(): WebRequestBuilder {
            val serviceFactory = ServiceFactory()
            return WebRequestBuilder(
                serviceFactory.distributionService(),
                serviceFactory.verificationService(),
                serviceFactory.submissionService(),
                VerificationKeys()
            )
        }
    }

    suspend fun asyncGetDateIndex(): List<String> = withContext(Dispatchers.IO) {
        return@withContext distributionService
            .getDateIndex(DiagnosisKeyConstants.AVAILABLE_DATES_URL).toList()
    }

    suspend fun asyncGetHourIndex(day: Date): List<String> = withContext(Dispatchers.IO) {
        return@withContext distributionService
            .getHourIndex(
                DiagnosisKeyConstants.AVAILABLE_DATES_URL +
                        "/${day.toServerFormat()}/${DiagnosisKeyConstants.HOUR}"
            )
            .toList()
    }

    /**
     * Retrieves Key Files from the Server based on a URL
     *
     * @param url the given URL
     */
    suspend fun asyncGetKeyFilesFromServer(
        url: String
    ): File = withContext(Dispatchers.IO) {
        val fileName = "${UUID.nameUUIDFromBytes(url.toByteArray())}.zip"
        val file = File(FileStorageHelper.keyExportDirectory, fileName)
        file.outputStream().use {
            Timber.v("Added $url to queue.")
            distributionService.getKeyFiles(url).byteStream().copyTo(it, DEFAULT_BUFFER_SIZE)
            Timber.v("key file request successful.")
        }
        return@withContext file
    }

    suspend fun asyncGetApplicationConfigurationFromServer(): ApplicationConfiguration =
        withContext(Dispatchers.IO) {
            var exportBinary: ByteArray? = null
            var exportSignature: ByteArray? = null

            distributionService.getApplicationConfiguration(
                DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL
            ).byteStream().unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) exportBinary = entryContent.copyOf()
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) exportSignature =
                    entryContent.copyOf()
            }
            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException()
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw ApplicationConfigurationCorruptException()
            }

            try {
                return@withContext ApplicationConfiguration.parseFrom(exportBinary)
            } catch (e: InvalidProtocolBufferException) {
                throw ApplicationConfigurationInvalidException()
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

    suspend fun asyncFakeGetRegistrationToken() = withContext(Dispatchers.IO) {
        verificationService.getRegistrationToken(
            SubmissionConstants.REGISTRATION_TOKEN_URL,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_REGISTRATION_TOKEN),
            RegistrationTokenRequest(
                requestPadding = requestPadding(
                    SubmissionConstants.PADDING_LENGTH_BODY_REGISTRATION_TOKEN_FAKE
                )
            )
        )
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

    suspend fun asyncFakeGetTestResult() = withContext(Dispatchers.IO) {
        verificationService.getTestResult(
            SubmissionConstants.TEST_RESULT_URL,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TEST_RESULT),
            RegistrationRequest(
                requestPadding = requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TEST_RESULT_FAKE)
            )
        )
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

    suspend fun asyncFakeGetTan() = withContext(Dispatchers.IO) {
        verificationService.getTAN(
            SubmissionConstants.TAN_REQUEST_URL,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TAN),
            TanRequestBody(requestPadding = requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TAN_FAKE))
        )
    }

    suspend fun asyncSubmitKeysToServer(
        authCode: String,
        keyList: List<KeyExportFormat.TemporaryExposureKey>
    ) = withContext(Dispatchers.IO) {
        Timber.d("Writing ${keyList.size} Keys to the Submission Payload.")
        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .build()
        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            authCode,
            "0",
            null,
            submissionPayload
        )
        return@withContext
    }

    suspend fun asyncFakeSubmitKeysToServer() = withContext(Dispatchers.IO) {

        val fakeKeys = generateSequence {
            KeyExportFormat.TemporaryExposureKey.newBuilder()
                .setKeyData(ByteString.copyFromUtf8("x".repeat(32)))
                .setRollingStartIntervalNumber(0)
                .setTransmissionRiskLevel(0)
                .setRollingPeriod(0)
                .build()
        }.take(14)

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(fakeKeys.toList())
            .build()
        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            null,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            submissionPayload
        )
    }

    private fun requestPadding(length: Int): String? = if (length == 0)
        null
    else
        "x".repeat(length)
}
