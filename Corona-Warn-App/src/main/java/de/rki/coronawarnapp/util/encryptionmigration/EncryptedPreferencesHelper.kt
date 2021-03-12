package de.rki.coronawarnapp.util.encryptionmigration

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class EncryptedPreferencesHelper @Inject constructor(
    private val applicationInfo: ApplicationInfo,
    factory: EncryptedPreferencesFactory
) {

    private val preferenceFile by lazy {
        File(applicationInfo.dataDir)
            .resolve("shared_prefs/$ENCRYPTED_SHARED_PREFERENCES_FILE.xml")
    }

    val instance: SharedPreferences? by lazy {
        if (preferenceFile.exists()) {
            factory.create(ENCRYPTED_SHARED_PREFERENCES_FILE)
        } else {
            null
        }
    }

    fun clean() {
        try {
            instance?.edit(true) {
                Timber.d("Clearing all encrypted preference values.")
                clear()
                Timber.d("Preference values have been cleared.")
            }
        } catch (e: Exception) {
            Timber.w("Failed to clear encrypted preferences.")
        }

        if (preferenceFile.exists()) {
            if (preferenceFile.delete()) {
                Timber.i("Encrypted preference file deleted.")
            } else {
                Timber.e("Encrypted preference could not be deleted.")
            }
        } else {
            Timber.d("Encrypted preference file did not exist.")
        }
    }

    companion object {
        private const val ENCRYPTED_SHARED_PREFERENCES_FILE = "shared_preferences_cwa"
    }
}
