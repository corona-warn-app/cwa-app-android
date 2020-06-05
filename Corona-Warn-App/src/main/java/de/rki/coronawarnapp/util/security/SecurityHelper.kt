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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.CwaSecurityException
import java.lang.Exception
import java.lang.NullPointerException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.security.Signature
import java.security.cert.Certificate

/**
 * Key Store and Password Access
 */
object SecurityHelper {
    private const val CWA_APP_SQLITE_DB_PW = "CWA_APP_SQLITE_DB_PW"
    private const val AES_KEY_SIZE = 256
    private const val SHARED_PREF_NAME = "shared_preferences_cwa"
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    private const val EXPORT_SIGNATURE_ALGORITHM = "SHA256withECDSA"
    private const val CWA_EXPORT_CERTIFICATE_NAME_NON_PROD = "cwa non-prod certificate"

    private const val CWA_EXPORT_CERTIFICATE_KEY_STORE = "trusted-certs-cwa.bks"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    private val androidKeyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).also {
            it.load(null)
        }
    }

    val globalEncryptedSharedPreferencesInstance: SharedPreferences by lazy {
        withSecurityCatch {
            CoronaWarnApplication.getAppContext().getEncryptedSharedPrefs(SHARED_PREF_NAME)
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
        .getInstance("SHA-256")
        .digest(input.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })

    fun exportFileIsValid(export: ByteArray?, sig: ByteArray?) = withSecurityCatch {
        Signature.getInstance(EXPORT_SIGNATURE_ALGORITHM).run {
            initVerify(trustedCertForSignature)
            update(export)
            verify(TEKSignatureList
                .parseFrom(sig)
                .signaturesList
                .first()
                .signature
                .toByteArray()
            )
        }
    }

    private val cwaKeyStore: KeyStore by lazy {
        val keystoreFile = CoronaWarnApplication.getAppContext()
            .assets.open(CWA_EXPORT_CERTIFICATE_KEY_STORE)
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        val keyStorePw = BuildConfig.TRUSTED_CERTS_EXPORT_KEYSTORE_PW
        val password = keyStorePw.toCharArray()
        if (password.isEmpty())
            throw NullPointerException("TRUSTED_CERTS_EXPORT_KEYSTORE_PW is null")
        keystore.load(keystoreFile, password)
        keystore
    }

    private val trustedCertForSignature: Certificate by lazy {
        val alias = CWA_EXPORT_CERTIFICATE_NAME_NON_PROD
        cwaKeyStore.getCertificate(alias)
    }

    private fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
