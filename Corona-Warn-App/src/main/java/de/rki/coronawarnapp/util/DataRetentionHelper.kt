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

import android.annotation.SuppressLint
import android.content.Context
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.util.security.SecurityHelper
import timber.log.Timber

/**
 * Helper for supplying functionality regarding Data Retention
 */
object DataRetentionHelper {
    private val TAG: String? = DataRetentionHelper::class.simpleName

    /**
     * Deletes all data known to the Application
     *
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    fun clearAllLocalData(context: Context) {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
        // Database Reset
        AppDatabase.reset(context)
        // Shared Preferences Reset
        SecurityHelper.resetSharedPrefs()
        // Reset the current risk level stored in LiveData
        RiskLevelRepository.reset()
        // Export File Reset
        FileStorageHelper.getAllFilesInKeyExportDirectory().forEach { it.delete() }
        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }
}
