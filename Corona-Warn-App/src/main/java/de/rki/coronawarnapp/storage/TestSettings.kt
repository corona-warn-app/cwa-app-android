package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
    }

    var isHourKeyPkgMode: Boolean
        get() {
            val value = prefs.getBoolean(PKEY_HOURLY_TESTING_MODE, false)
            return value && CWADebug.isDeviceForTestersBuild
        }
        set(value) = prefs.edit {
            putBoolean(PKEY_HOURLY_TESTING_MODE, value)
        }

    companion object {
        private const val PKEY_HOURLY_TESTING_MODE = "diagnosiskeys.hourlytestmode"
    }
}
