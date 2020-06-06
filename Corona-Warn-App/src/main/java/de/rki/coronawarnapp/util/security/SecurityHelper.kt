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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.security.SecurityConstants.AES_KEY_SIZE
import de.rki.coronawarnapp.util.security.SecurityConstants.ANDROID_KEY_STORE
import de.rki.coronawarnapp.util.security.SecurityConstants.DIGEST_ALGORITHM
import de.rki.coronawarnapp.util.security.SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Key Store and Password Access
 */
object SecurityHelper {
    private const val CWA_APP_SQLITE_DB_PW = "CWA_APP_SQLITE_DB_PW"

    private val TAG = SecurityHelper::class.simpleName

    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    private val verificationKeys = VerificationKeys()

    private val androidKeyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).also {
            it.load(null)
        }
    }

    val globalEncryptedSharedPreferencesInstance: SharedPreferences by lazy {
        withSecurityCatch {
            CoronaWarnApplication.getAppContext().getEncryptedSharedPrefs(ENCRYPTED_SHARED_PREFERENCES_FILE)
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

    /**
     * Retrieves the Master Key from the Android KeyStore to use in SQLCipher
     */
    fun getDBPassword() = getOrGenerateDBSecretKey()
        .toString()
        .toCharArray()

    private fun getOrGenerateDBSecretKey(): SecretKey =
        androidKeyStore.getKey(CWA_APP_SQLITE_DB_PW, null).run {
            return if (this == null) {
                val kg: KeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE
                )
                val spec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                    CWA_APP_SQLITE_DB_PW,
                    KeyProperties.PURPOSE_ENCRYPT and KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(AES_KEY_SIZE)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)
                    .build()
                kg.init(spec, SecureRandom())
                kg.generateKey()
            } else this as SecretKey
        }

    fun hash256(input: String): String = MessageDigest
        .getInstance(DIGEST_ALGORITHM)
        .digest(input.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })

    fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
