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

/**
 * The Diagnosis Key constants
 */
object DiagnosisKeyConstants {
    /** version resource variable for REST-like Service Calls */
    private const val VERSION = "version"

    /** diagnosis keys resource variable for REST-like Service Calls */
    private const val DIAGNOSIS_KEYS = "diagnosis-keys"

    /** resource variables but non-static context */
    private var CURRENT_VERSION = "v1"

    /** Submission URL built from CDN URL's and REST resources */
    private var VERSIONED_SUBMISSION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    /** Diagnosis key Submission URL built from CDN URL's and REST resources */
    val DIAGNOSIS_KEYS_SUBMISSION_URL = "$VERSIONED_SUBMISSION_CDN_URL/$DIAGNOSIS_KEYS"

    const val SERVER_ERROR_CODE_403 = 403
}
