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

    /** parameter resource variable for REST-like Service Calls */
    private const val PARAMETERS = "parameters"
    private const val APPCONFIG = "configuration"

    /** diagnosis keys resource variable for REST-like Service Calls */
    private const val DIAGNOSIS_KEYS = "diagnosis-keys"

    /** country resource variable for REST-like Service Calls */
    private const val COUNTRY = "country"

    /** date resource variable for REST-like Service Calls */
    const val DATE = "date"

    /** hour resource variable for REST-like Service Calls */
    const val HOUR = "hour"

    private const val INDEX_FILE_NAME = "index.txt"

    /** resource variables but non-static context */
    private var CURRENT_VERSION = "v1"
    private const val CURRENT_COUNTRY = "DE"

    /** Distribution URL built from CDN URL's and REST resources */
    private var VERSIONED_DISTRIBUTION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    /** Submission URL built from CDN URL's and REST resources */
    private var VERSIONED_SUBMISSION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    /** Parameter Download URL built from CDN URL's and REST resources */
    private val PARAMETERS_DOWNLOAD_URL = "$VERSIONED_DISTRIBUTION_CDN_URL/$PARAMETERS"
    private val APPCONFIG_DOWNLOAD_URL = "$VERSIONED_DISTRIBUTION_CDN_URL/$APPCONFIG"

    /** Index Download URL built from CDN URL's and REST resources */
    val INDEX_DOWNLOAD_URL = "$VERSIONED_DISTRIBUTION_CDN_URL/$INDEX_FILE_NAME"

    /** Diagnosis key Download URL built from CDN URL's and REST resources */
    val DIAGNOSIS_KEYS_DOWNLOAD_URL = "$VERSIONED_DISTRIBUTION_CDN_URL/$DIAGNOSIS_KEYS"

    /** Diagnosis key Submission URL built from CDN URL's and REST resources */
    val DIAGNOSIS_KEYS_SUBMISSION_URL = "$VERSIONED_SUBMISSION_CDN_URL/$DIAGNOSIS_KEYS"

    /** Country-Specific Parameter URL built from CDN URL's and REST resources */
    val PARAMETERS_COUNTRY_DOWNLOAD_URL = "$PARAMETERS_DOWNLOAD_URL/$COUNTRY"
    val APPCONFIG_COUNTRY_DOWNLOAD_URL = "$APPCONFIG_DOWNLOAD_URL/$COUNTRY"

    val COUNTRY_APPCONFIG_DOWNLOAD_URL =
        "$APPCONFIG_COUNTRY_DOWNLOAD_URL/$CURRENT_COUNTRY/app_config"

    /** Dynamic URL to be able to specify which country should be used
     * instead of $CURRENT_COUNTRY
     **/
    val AVAILABLE_COUNTRIES_URL = "$DIAGNOSIS_KEYS_DOWNLOAD_URL/$COUNTRY"

    /** Available Dates URL built from CDN URL's and REST resources */
    val AVAILABLE_DATES_URL = "$AVAILABLE_COUNTRIES_URL/$CURRENT_COUNTRY/$DATE"

    const val SERVER_ERROR_CODE_403 = 403
}
