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
import android.util.Log
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.http.requests.RegistrationTokenRequest
import de.rki.coronawarnapp.http.requests.ReqistrationRequest
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SecurityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.UUID

object WebRequestBuilder {
    private val TAG: String? = WebRequestBuilder::class.simpleName

    private const val EXPORT_BINARY_FILE_NAME = "export.bin"
    private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

    private val serviceFactory = ServiceFactory()

    private val distributionService = serviceFactory.distributionService()
    private val verificationService = serviceFactory.verificationService()
    private val submissionService = serviceFactory.submissionService()

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
        val requestID = UUID.randomUUID()
        val fileName = "${UUID.nameUUIDFromBytes(url.toByteArray())}.zip"
        val file = File(FileStorageHelper.keyExportDirectory, fileName)
        file.outputStream().use {
            Log.v(requestID.toString(), "Added $url to queue.")
            distributionService.getKeyFiles(url).byteStream().copyTo(it, DEFAULT_BUFFER_SIZE)
            Log.v(requestID.toString(), "key file request successful.")
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
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) exportSignature = entryContent.copyOf()
            }
            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException()
            }

            if (!SecurityHelper.exportFileIsValid(exportBinary, exportSignature)) {
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
        keyType: String
    ): String = withContext(Dispatchers.IO) {
        val keyStr = if (keyType == SubmissionConstants.QR_CODE_KEY_TYPE) {
            SecurityHelper.hash256(key)
        } else {
            key
        }
        verificationService.getRegistrationToken(
            SubmissionConstants.REGISTRATION_TOKEN_URL,
            "0",
            RegistrationTokenRequest(keyType, keyStr)
        ).registrationToken
    }

    suspend fun asyncGetTestResult(
        registrationToken: String
    ): Int = withContext(Dispatchers.IO) {
        verificationService.getTestResult(
            SubmissionConstants.TEST_RESULT_URL,
            "0", ReqistrationRequest(registrationToken)
        ).testResult
    }

    suspend fun asyncGetTan(
        registrationToken: String
    ): String = withContext(Dispatchers.IO) {
        verificationService.getTAN(SubmissionConstants.TAN_REQUEST_URL, "0",
            TanRequestBody(
                registrationToken
            )
        ).tan
    }

    suspend fun asyncSubmitKeysToServer(
        authCode: String,
        faked: Boolean,
        keyList: List<KeyExportFormat.TemporaryExposureKey>
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Writing ${keyList.size} Keys to the Submission Payload.")
        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .build()
        var fakeHeader = "0"
        if (faked) fakeHeader = Math.random().toInt().toString()
        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            authCode,
            fakeHeader,
            submissionPayload
        )
        return@withContext
    }
}
