package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.util.security.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.security.SecurityConstants
import java.io.File
import javax.inject.Inject

class EncryptedPreferencesHelper @Inject constructor(
    private val applicationInfo: ApplicationInfo,
    factory: EncryptedPreferencesFactory,
    encryptionErrorResetTool: EncryptionErrorResetTool
) {

    private val encryptedPreferencesFile by lazy {
        File(applicationInfo.dataDir)
            .resolve("shared_prefs/${SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE}.xml")
    }

    val encryptedSharedPreferencesInstance: SharedPreferences? by lazy {
        withSecurityCatch {
            try {
                if (encryptedPreferencesFile.exists()) {
                    factory.create(SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE)
                } else {
                    null
                }
            } catch (e: Exception) {
                encryptionErrorResetTool.isResetNoticeToBeShown = true
                null
            }
        }
    }

    fun clean() {
        encryptedSharedPreferencesInstance?.edit(true) {
            clear()
        }
        encryptedPreferencesFile.delete()
    }

    private fun <T> withSecurityCatch(doInCatch: () -> T) = try {
        doInCatch.invoke()
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }
}
