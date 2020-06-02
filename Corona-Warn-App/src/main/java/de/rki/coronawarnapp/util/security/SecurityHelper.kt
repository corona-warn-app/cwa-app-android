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

package de.rki.coronawarnapp.util.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.http.OkHttp3Stack
import java.security.KeyStore

/**
 * Key Store and Password Access
 */
object SecurityHelper {
    private const val SHARED_PREF_NAME = "shared_preferences_cwa"
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    private const val AndroidKeyStore = "AndroidKeyStore"

    val keyStore: KeyStore by lazy {
        KeyStore.getInstance(AndroidKeyStore).also {
            it.load(null)
        }
    }

    val globalEncryptedSharedPreferencesInstance: SharedPreferences by lazy {
        CoronaWarnApplication.getAppContext().getEncryptedSharedPrefs(SHARED_PREF_NAME)
    }

    /**
     * Initializes the private encrypted key store
     */
    private fun Context.getEncryptedSharedPrefs(fileName: String) = EncryptedSharedPreferences
        .create(
            fileName,
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    /**
     * Retrieves the Master Key from the Android KeyStore to use in SQLCipher
     */
    fun getDBPassword() = keyStore
        .getKey(masterKeyAlias, null)
        .toString()
        .toCharArray()

    fun getPinnedWebStack(appContext: Context): RequestQueue {
        return Volley.newRequestQueue(appContext, OkHttp3Stack(appContext))
    }
}
