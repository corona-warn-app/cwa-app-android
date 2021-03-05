package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.util.security.SecurityConstants
import java.io.File
import javax.inject.Inject

class EncryptedPreferencesHelper @Inject constructor(
    @EncryptedPreferences private val sharedPreferences: SharedPreferences?,
    applicationInfo: ApplicationInfo
) {

    private val file = File(applicationInfo.dataDir)
        .resolve("shared_prefs/${SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE}.xml")

    fun isAvailable(): Boolean = file.exists()

    fun clean() {
        sharedPreferences?.edit(true) {
            clear()
        }
        file.delete()
    }
}
