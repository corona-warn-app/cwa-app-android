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

package de.rki.coronawarnapp.service.diagnosiskey

import KeyExportFormat
import de.rki.coronawarnapp.exception.DiagnosisKeyRetrievalException
import de.rki.coronawarnapp.exception.DiagnosisKeySubmissionException
import de.rki.coronawarnapp.http.WebRequestBuilder
import timber.log.Timber

/**
 * The Diagnosis Key Service is used to interact with the Server to submit and retrieve keys through
 * predefined structures.
 *
 * @throws DiagnosisKeyRetrievalException An Exception thrown when an error occurs during Key Retrieval from the Server
 * @throws DiagnosisKeySubmissionException An Exception thrown when an error occurs during Key Reporting to the Server
 */
object DiagnosisKeyService {

    private val TAG: String? = DiagnosisKeyService::class.simpleName

    /**
     * Asynchronously submits keys to the Server with the WebRequestBuilder by retrieving
     * keys out of the Google API.
     *
     *
     * @throws de.rki.coronawarnapp.exception.DiagnosisKeySubmissionException An Exception thrown when an error occurs during Key Reporting to the Server
     *
     * @param authCode - TAN Authorization Code used to validate the request
     * @param keysToReport - KeyList in the Server Format to submit to the Server
     */
    suspend fun asyncSubmitKeys(
        authCode: String,
        keysToReport: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        Timber.d("Diagnosis Keys will be submitted.")
        WebRequestBuilder.getInstance().asyncSubmitKeysToServer(
            authCode,
            false,
            keysToReport
        )
    }
}
