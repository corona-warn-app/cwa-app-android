package de.rki.coronawarnapp.main

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import org.joda.time.Instant
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

    var firstReliableDeviceTime: Instant
        get() = Instant.ofEpochMilli(prefs.getLong(PKEY_DEVICE_TIME_FIRST_RELIABLE, 0L))
        set(value) = prefs.edit { putLong(PKEY_DEVICE_TIME_FIRST_RELIABLE, value.millis) }

    var lastDeviceTimeStateChangeAt: Instant
        get() = Instant.ofEpochMilli(prefs.getLong(PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME, 0L))
        set(value) = prefs.edit { putLong(PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME, value.millis) }

    var lastDeviceTimeStateChangeState: ConfigData.DeviceTimeState
        get() = prefs.getString(
            PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE,
            ConfigData.DeviceTimeState.INCORRECT.key
        ).let { raw -> ConfigData.DeviceTimeState.values().single { it.key == raw } }
        set(value) = prefs.edit { putString(PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE, value.key) }

    val lastChangelogVersion = prefs.createFlowPreference(
        key = LAST_CHANGELOG_VERSION,
        defaultValue = DEFAULT_APP_VERSION
    )

    fun clear() {
        prefs.clearAndNotify()
    }

    companion object {
        private const val PKEY_DEVICE_TIME_INCORRECT_ACK = "devicetime.incorrect.acknowledged"
        private const val PKEY_DEVICE_TIME_FIRST_RELIABLE = "devicetime.correct.first"
        private const val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME = "devicetime.laststatechange.timestamp"
        private const val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE = "devicetime.laststatechange.state"
        private const val LAST_CHANGELOG_VERSION = "update.changelog.lastversion"
        private const val DEFAULT_APP_VERSION = 1L
    }
}
