package de.rki.coronawarnapp.util.security

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.security.SecurityConstants.CWA_APP_SQLITE_DB_PW
import de.rki.coronawarnapp.util.security.SecurityConstants.DB_PASSWORD_MAX_LENGTH
import de.rki.coronawarnapp.util.security.SecurityConstants.DB_PASSWORD_MIN_LENGTH
import de.rki.coronawarnapp.util.security.SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE
import timber.log.Timber
import java.security.SecureRandom

/**
 * Key Store and Password Access
 */
object SecurityHelper {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val encryptedPreferencesProvider: (ApplicationComponent) -> SharedPreferences = {
        val factory = it.encryptedPreferencesFactory
        val encryptionErrorResetTool = it.errorResetTool
        withSecurityCatch {
            try {
                factory.create(ENCRYPTED_SHARED_PREFERENCES_FILE)
            } catch (e: Exception) {
                if (encryptionErrorResetTool.tryResetIfNecessary(e)) {
                    Timber.w("We could recovery from this error via reset. Now retrying.")
                    factory.create(ENCRYPTED_SHARED_PREFERENCES_FILE)
                } else {
                    throw e
                }
            }
        }
    }

    val globalEncryptedSharedPreferencesInstance: SharedPreferences by lazy {
        encryptedPreferencesProvider(AppInjector.component)
    }

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
        globalEncryptedSharedPreferencesInstance.clearAndNotify()
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
