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

    var wasTracingExplanationDialogShown: Boolean
        get() = prefs.getBoolean(PKEY_TRACING_DIALOG_SHOWN, false)
        set(value) = prefs.edit { putBoolean(PKEY_TRACING_DIALOG_SHOWN, value) }

    var wasInteroperabilityShownAtLeastOnce: Boolean
        get() = prefs.getBoolean(PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE, false)
        set(value) = prefs.edit { putBoolean(PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE, value) }

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

    var numberOfRemainingSharePositiveTestResultRemindersPcr: Int
        get() = prefs.getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR, Int.MIN_VALUE)
        set(value) = prefs.edit { putInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR, value) }

    var numberOfRemainingSharePositiveTestResultRemindersRat: Int
        get() = prefs.getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT, Int.MIN_VALUE)
        set(value) = prefs.edit { putInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT, value) }

    val isNotificationsRiskEnabled = prefs.createFlowPreference(
        key = PKEY_NOTIFICATIONS_RISK_ENABLED,
        defaultValue = true
    )

    val isNotificationsTestEnabled = prefs.createFlowPreference(
        key = PKEY_NOTIFICATIONS_TEST_ENABLED,
        defaultValue = true
    )

    val lastChangelogVersion = prefs.createFlowPreference(
        key = LAST_CHANGELOG_VERSION,
        defaultValue = DEFAULT_APP_VERSION
    )

    fun clear() {
        prefs.clearAndNotify()
    }

    companion object {
        private const val PKEY_DEVICE_TIME_INCORRECT_ACK = "devicetime.incorrect.acknowledged"
        private const val PKEY_TRACING_DIALOG_SHOWN = "tracing.dialog.shown"
        private const val PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE = "interoperability.showed"
        private const val PKEY_DEVICE_TIME_FIRST_RELIABLE = "devicetime.correct.first"
        private const val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME =
            "devicetime.laststatechange.timestamp"
        private const val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE =
            "devicetime.laststatechange.state"
        private const val PKEY_NOTIFICATIONS_RISK_ENABLED = "notifications.risk.enabled"
        private const val PKEY_NOTIFICATIONS_TEST_ENABLED = "notifications.test.enabled"

        private const val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR = "testresults.count"
        private const val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT = "testresults.count.rat"

        private const val LAST_CHANGELOG_VERSION = "update.changelog.lastversion"
        private const val DEFAULT_APP_VERSION = 1L
    }
}
