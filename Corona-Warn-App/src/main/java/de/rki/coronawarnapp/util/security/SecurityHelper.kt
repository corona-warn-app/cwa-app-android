package de.rki.coronawarnapp.util.security

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
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

    private fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
