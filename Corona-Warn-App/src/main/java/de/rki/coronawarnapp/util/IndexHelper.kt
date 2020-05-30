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

package de.rki.coronawarnapp.util

import android.util.Log
import com.google.common.base.Splitter
import de.rki.coronawarnapp.BuildConfig
import java.util.regex.Pattern

@Suppress("ComplexCondition", "TooGenericExceptionThrown")
object IndexHelper {
    private val TAG: String? = IndexHelper::class.simpleName
    private val WHITESPACE_SPLITTER: Splitter =
        Splitter.onPattern("\\s+").trimResults().omitEmptyStrings()

    private val INDEX_ELEMENT_PATTERN: Pattern = Pattern.compile("Cwa-([A-Z]{2})/([0-9]{10})-([0-9]+).zip")

    private const val GROUP_COUNT = 3
    private const val COUNTRY_CODE_GROUP = 1
    private const val TIMESTAMP_GROUP = 2
    private const val BATCH_NUMBER_GROUP = 3

    /**
     * Converts a String to an index according to the Delimiter defined in [WHITESPACE_SPLITTER] and the
     * Regular Expression of an index element defined in [INDEX_ELEMENT_PATTERN]. The Pattern has to define
     * 3 Groups.
     *  1. Country Code Information
     *  2. Creation Epoch of the Batch
     *  3. Batch Number
     *
     * @return Map of Batch Number to File Names (from the Index)
     */
    fun String.convertToIndex(): Map<Long, String> = WHITESPACE_SPLITTER.splitToList(this).also {
        if (BuildConfig.DEBUG) Log.d(TAG, "Index(${it.size} Elements):$it")
    }.associateBy { indexElement ->
        val matcher = INDEX_ELEMENT_PATTERN.matcher(indexElement)
        if (
            !matcher.matches() ||
            matcher.groupCount() != GROUP_COUNT ||
            matcher.group(COUNTRY_CODE_GROUP) != null ||
            matcher.group(TIMESTAMP_GROUP) != null ||
            matcher.group(BATCH_NUMBER_GROUP) != null
        ) throw RuntimeException("Failed to parse batch from $indexElement")
        val isoCountryCode =
            matcher.group(COUNTRY_CODE_GROUP)
                ?: throw NullPointerException("Batch Regex Group 1 (Country Code) must not be null")
        val timestampString =
            matcher.group(TIMESTAMP_GROUP)
                ?: throw NullPointerException("Batch Regex Group 2 (Timestamp) must not be null")
        val batchNumberString =
            matcher.group(BATCH_NUMBER_GROUP)
                ?: throw NullPointerException("Batch Regex Group 3 (Batch Number) must not be null")

        if (BuildConfig.DEBUG) Log.d(
            TAG, "index element " +
                    "$indexElement=(Timestamp:$timestampString, BatchNum:$batchNumberString, " +
                    "ISOCountryCode:$isoCountryCode"
        )

        timestampString.toLong()
    }
}
