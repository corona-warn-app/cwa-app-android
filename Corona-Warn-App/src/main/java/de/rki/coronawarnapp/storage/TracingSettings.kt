package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantMidnightUtc
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import org.joda.time.LocalDate
import timber.log.Timber
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

    @Deprecated("Use CoronaTestRepository")
    var initialPollingForTestResultTimeStampMigration: Long
        get() = prefs.getLong(TRACING_POOLING_TIMESTAMP, 0L)
        set(value) = prefs.edit(true) {
            putLong(TRACING_POOLING_TIMESTAMP, value)
        }

    @Deprecated("Use CoronaTestRepository")
    var isTestResultAvailableNotificationSentMigration: Boolean
        get() = prefs.getBoolean(TEST_RESULT_NOTIFICATION_SENT, false)
        set(value) = prefs.edit(true) {
            putBoolean(TEST_RESULT_NOTIFICATION_SENT, value)
        }

    val isUserToBeNotifiedOfLoweredRiskLevel = prefs.createFlowPreference(
        key = LOWERED_RISK_LEVEL,
        defaultValue = false
    )

    val isUserToBeNotifiedOfAdditionalHighRiskLevel = prefs.createFlowPreference(
        key = ADDITIONAL_HIGH_RISK_LEVEL,
        defaultValue = false
    )

    var lastHighRiskDate: LocalDate?
        get() = prefs.getLong(LAST_HIGH_RISK_LOCALDATE, 0L).toInstantOrNull()?.toLocalDateUtc()
        set(value) = prefs.edit(true) {
            putLong(LAST_HIGH_RISK_LOCALDATE, value?.toInstantMidnightUtc()?.millis ?: 0L)
        }

    /**
     * A flag to show a badge in home screen when risk level changes from Low to High or vice versa
     */
    val showRiskLevelBadge = prefs.createFlowPreference(
        key = PKEY_SHOW_RISK_LEVEL_BADGE,
        defaultValue = false
    )

    fun deleteLegacyTestData() {
        Timber.d("deleteLegacyTestData()")
        prefs.edit {
            remove(TEST_RESULT_NOTIFICATION_SENT)
            remove(TRACING_POOLING_TIMESTAMP)
        }
    }

    fun clear() = prefs.clearAndNotify()

    companion object {
        const val TRACING_POOLING_TIMESTAMP = "tracing.pooling.timestamp"
        const val TRACING_ACTIVATION_TIMESTAMP = "tracing.activation.timestamp"
        const val TEST_RESULT_NOTIFICATION_SENT = "test.notification.sent"
        const val LOWERED_RISK_LEVEL = "notification.risk.lowered"
        const val ADDITIONAL_HIGH_RISK_LEVEL = "notification.risk.additionalhigh"
        const val LAST_HIGH_RISK_LOCALDATE = "tracing.lasthighrisk.localdate"
        private const val PKEY_SHOW_RISK_LEVEL_BADGE = "notifications.risk.level.change.badge"
    }
}
