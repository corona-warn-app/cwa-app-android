package de.rki.coronawarnapp.diagnosiskeys.storage

import de.rki.coronawarnapp.util.security.SecurityHelper
import javax.inject.Inject

class DownloadDiagnosisKeysSettings @Inject constructor() {

    /**
     * true if, and only if, since last runtime the app was updated from 1.7 (or earlier) to a version >= 1.8.0
     */
    val isUpdateToEnfV2: Boolean
        get() = lastVersionCode < VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE

    var lastVersionCode: Int
        get() =
            SecurityHelper.globalEncryptedSharedPreferencesInstance.getInt(KEY_LAST_VERSION_CODE, -1)
        set(value) =
            SecurityHelper.globalEncryptedSharedPreferencesInstance.edit().putInt(KEY_LAST_VERSION_CODE, value).apply()

    companion object {
        private const val KEY_LAST_VERSION_CODE = "KEY_LAST_VERSION_CODE"
        private const val VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE = 1080000
    }
}
