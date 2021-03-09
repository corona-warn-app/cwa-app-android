package de.rki.coronawarnapp.util.security

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Base64
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.security.SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE

/**
 * Key Store and Password Access
 */
object SecurityHelper {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val encryptedPreferencesProvider: (ApplicationComponent) -> SharedPreferences? = {
        val factory = it.encryptedPreferencesFactory
        val encryptionErrorResetTool = it.errorResetTool
        withSecurityCatch {
            try {
                factory.create(ENCRYPTED_SHARED_PREFERENCES_FILE)
            } catch (e: Exception) {
                encryptionErrorResetTool.isResetNoticeToBeShown = true
                null
            }
        }
    }

    val globalEncryptedSharedPreferencesInstance: SharedPreferences? by lazy {
        encryptedPreferencesProvider(AppInjector.component)
    }

    private val String.toPreservedByteArray: ByteArray
        get() = Base64.decode(this, Base64.NO_WRAP)

    private val ByteArray.toPreservedString: String
        get() = Base64.encodeToString(this, Base64.NO_WRAP)

    @SuppressLint("ApplySharedPref")
    fun resetSharedPrefs() {
        globalEncryptedSharedPreferencesInstance?.clearAndNotify()
    }

    fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
