package de.rki.coronawarnapp.storage.preferences

import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import de.rki.coronawarnapp.util.security.SecurityConstants
import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
import java.io.File
import javax.inject.Inject

class EncryptedPreferencesHelper @Inject constructor(applicationInfo: ApplicationInfo) {

    private val file = File(applicationInfo.dataDir)
        .resolve("shared_prefs/${SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE}.xml")

    fun isAvailable(): Boolean = file.exists()

    fun clean() {
        globalEncryptedSharedPreferencesInstance?.edit(true) {
            clear()
        }
        file.delete()
    }
}
