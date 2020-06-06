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

import KeyExportFormat.TEKSignatureList
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.security.SecurityConstants.AES_KEY_SIZE
import de.rki.coronawarnapp.util.security.SecurityConstants.ANDROID_KEY_STORE
import de.rki.coronawarnapp.util.security.SecurityConstants.DIGEST_ALGORITHM
import de.rki.coronawarnapp.util.security.SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE
import de.rki.coronawarnapp.util.security.SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM
import de.rki.coronawarnapp.util.security.SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_PUBLIC_KEY_FILES
import java.lang.Exception
import java.security.KeyFactory
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Scanner

/**
 * Key Store and Password Access
 */
object SecurityHelper {
    private const val CWA_APP_SQLITE_DB_PW = "CWA_APP_SQLITE_DB_PW"

    private val TAG = SecurityHelper::class.simpleName

    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

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

    fun exportFileIsValid(export: ByteArray?, signatureListBinary: ByteArray?) = withSecurityCatch {
        Signature.getInstance(EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM).run {
            val validSignatures = publicKeysForVerification
                .onEach { key -> Log.v(TAG, "verify with $key") }
                .flatMap { key ->
                    initVerify(key)
                    update(export)
                    TEKSignatureList.parseFrom(signatureListBinary).signaturesList
                        .onEach { Log.v(TAG, "verify $it") }
                        .mapNotNull { it.signature }
                        .map { it.toByteArray() }
                        .filter { signatureBinary -> verify(signatureBinary) }
                }
            Log.v(TAG, "${validSignatures.size} valid signatures")
            return@run validSignatures.isNotEmpty()
        }
    }

    private val publicKeysForVerification: List<PublicKey> by lazy {
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        val pubKeyBinary = CoronaWarnApplication.getAppContext()
            .assets.open(EXPORT_FILE_SIGNATURE_VERIFICATION_PUBLIC_KEY_FILES)
        val scanner = Scanner(pubKeyBinary)
        val keys = mutableListOf<PublicKey>()
        while (scanner.hasNextLine()) {
            keys.add(keyFactory.generatePublic(X509EncodedKeySpec(decode(scanner.nextLine(), DEFAULT))))
        }
        keys
    }

    private fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
