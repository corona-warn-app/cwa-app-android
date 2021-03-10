package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TracingSettings @Inject constructor(@AppContext private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("tracing_settings", Context.MODE_PRIVATE)
    }

    var isConsentGiven: Boolean
        get() = prefs.getBoolean(TRACING_ACTIVATION_TIMESTAMP, false)
        set(value) = prefs.edit(true) {
            putBoolean(TRACING_ACTIVATION_TIMESTAMP, value)
        }

    var initialPollingForTestResultTimeStamp: Long
        get() = prefs.getLong(TRACING_POOLING_TIMESTAMP, 0L)
        set(value) = prefs.edit(true) {
            putLong(TRACING_POOLING_TIMESTAMP, value)
        }

    var isTestResultAvailableNotificationSent: Boolean
        get() = prefs.getBoolean(TEST_RESULT_NOTIFICATION_SENT, false)
        set(value) = prefs.edit(true) {
            putBoolean(TEST_RESULT_NOTIFICATION_SENT, value)
        }

    val isUserToBeNotifiedOfLoweredRiskLevel = prefs.createFlowPreference(
        key = LOWERED_RISK_LEVEL,
        defaultValue = false
    )

    companion object {
        const val TRACING_POOLING_TIMESTAMP = "tracing.pooling.timestamp"
        const val TRACING_ACTIVATION_TIMESTAMP = "tracing.activation.timestamp"
        const val TEST_RESULT_NOTIFICATION_SENT = "test.notification.sent"
        const val LOWERED_RISK_LEVEL = "notification.risk.lowered"
    }
}
