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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.security.SecurityConstants.CWA_APP_SQLITE_DB_PW
import de.rki.coronawarnapp.util.security.SecurityConstants.DB_PASSWORD_MAX_LENGTH
import de.rki.coronawarnapp.util.security.SecurityConstants.DB_PASSWORD_MIN_LENGTH
import de.rki.coronawarnapp.util.security.SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE
import java.security.SecureRandom

/**
 * Key Store and Password Access
 */
object SecurityHelper {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    val globalEncryptedSharedPreferencesInstance: SharedPreferences by lazy {
        withSecurityCatch {
            CoronaWarnApplication.getAppContext()
                .getEncryptedSharedPrefs(ENCRYPTED_SHARED_PREFERENCES_FILE)
        }
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

    private val String.toPreservedByteArray: ByteArray
        get() = Base64.decode(this, Base64.NO_WRAP)

    private val ByteArray.toPreservedString: String
        get() = Base64.encodeToString(this, Base64.NO_WRAP)

    /**
     * Retrieves the db password from encrypted shared preferences. The password is automatically
     * encrypted when storing the password and decrypted when retrieving the password.
     *
     * If no password exists yet, a new password is generated and stored into
     * encrypted shared preferences. The length of the password will be in the interval
     * of [[DB_PASSWORD_MIN_LENGTH], [DB_PASSWORD_MAX_LENGTH]]
     */
    fun getDBPassword(): ByteArray =
        getStoredDbPassword() ?: storeDbPassword(generateDBPassword())

    @SuppressLint("ApplySharedPref")
    fun resetSharedPrefs() {
        globalEncryptedSharedPreferencesInstance.edit().clear().commit()
    }

    private fun getStoredDbPassword(): ByteArray? =
        globalEncryptedSharedPreferencesInstance
            .getString(CWA_APP_SQLITE_DB_PW, null)?.toPreservedByteArray

    private fun storeDbPassword(keyBytes: ByteArray): ByteArray {
        globalEncryptedSharedPreferencesInstance
            .edit()
            .putString(CWA_APP_SQLITE_DB_PW, keyBytes.toPreservedString)
            .apply()
        return keyBytes
    }

    private fun generateDBPassword(): ByteArray {
        val secureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            SecureRandom()
        }
        val max = DB_PASSWORD_MAX_LENGTH
        val min = DB_PASSWORD_MIN_LENGTH
        val passwordLength = secureRandom.nextInt(max - min + 1) + min
        val password = ByteArray(passwordLength)
        secureRandom.nextBytes(password)
        return password
    }

    fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
