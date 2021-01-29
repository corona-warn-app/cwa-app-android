package de.rki.coronawarnapp.main

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import javax.inject.Inject

/**
 * For general app related values,
 * e.g. "Has dialog been shown", as "OnBoarding been shown?"
 * In future refactoring it should contain all values
 * from **[de.rki.coronawarnapp.storage.LocalData]** that don't fit more specific classes.
 */
class CWASettings @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("cwa_main_localdata", Context.MODE_PRIVATE)
    }

    var wasDeviceTimeIncorrectAcknowledged: Boolean
        get() = prefs.getBoolean(PKEY_DEVICE_TIME_INCORRECT_ACK, false)
        set(value) = prefs.edit { putBoolean(PKEY_DEVICE_TIME_INCORRECT_ACK, value) }

    fun clear() {
        prefs.clearAndNotify()
    }

    companion object {
        private const val PKEY_DEVICE_TIME_INCORRECT_ACK = "devicetime.incorrect.acknowledged"
    }
}
