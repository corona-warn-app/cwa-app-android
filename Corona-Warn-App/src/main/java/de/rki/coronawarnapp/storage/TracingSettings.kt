package de.rki.coronawarnapp.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantMidnightUtc
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.getValueOrDefault
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TracingSettings @Inject constructor(
    @StorageDataStore private val dataStore: DataStore<Preferences>
) {

    suspend fun isConsentGiven() = dataStore.getValueOrDefault(TRACING_ACTIVATION_TIMESTAMP, false)

    suspend fun updateConsentGiven(isConsentGiven: Boolean) = dataStore.trySetValue(
        preferencesKey = TRACING_ACTIVATION_TIMESTAMP,
        value = isConsentGiven
    )

    @Deprecated("Use CoronaTestRepository")
    suspend fun isTestResultAvailableNotificationSentMigration() = dataStore.getValueOrDefault(
        preferencesKey = TEST_RESULT_NOTIFICATION_SENT,
        defaultValue = false
    )

    @Deprecated("Use CoronaTestRepository")
    suspend fun updateTestResultAvailableNotificationSentMigration(sent: Boolean) = dataStore.trySetValue(
        preferencesKey = TEST_RESULT_NOTIFICATION_SENT,
        value = sent
    )

    val isUserToBeNotifiedOfLoweredRiskLevel = dataStore.dataRecovering.distinctUntilChanged(
        key = LOWERED_RISK_LEVEL,
        defaultValue = false
    )

    suspend fun updateUserToBeNotifiedOfLoweredRiskLevel(notify: Boolean) = dataStore.trySetValue(
        preferencesKey = LOWERED_RISK_LEVEL,
        value = notify
    )

    val isUserToBeNotifiedOfAdditionalHighRiskLevel = dataStore.dataRecovering.distinctUntilChanged(
        key = ADDITIONAL_HIGH_RISK_LEVEL,
        defaultValue = false
    )

    suspend fun updateUserToBeNotifiedOfAdditionalHighRiskLevel(notify: Boolean) = dataStore.trySetValue(
        preferencesKey = ADDITIONAL_HIGH_RISK_LEVEL,
        value = notify
    )

    val lastHighRiskDate = dataStore.dataRecovering
        .map(LAST_HIGH_RISK_LOCALDATE)
        .map { it?.toInstantOrNull()?.toLocalDateUtc() }
        .distinctUntilChanged()

    suspend fun updateLastHighRiskDate(date: LocalDate?) = dataStore.trySetValue(
        preferencesKey = LAST_HIGH_RISK_LOCALDATE,
        value = date?.toInstantMidnightUtc()?.millis ?: 0L
    )

    /**
     * A flag to show a badge in home screen when risk level changes from Low to High or vice versa
     */
    val showRiskLevelBadge = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_SHOW_RISK_LEVEL_BADGE,
        defaultValue = false
    )

    suspend fun updateShowRiskLevelBadge(show: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_SHOW_RISK_LEVEL_BADGE,
        value = show
    )

    suspend fun deleteLegacyTestData() {
        Timber.d("deleteLegacyTestData()")
        dataStore.edit { prefs -> prefs.remove(TEST_RESULT_NOTIFICATION_SENT) }
    }

    companion object {
        private val TRACING_ACTIVATION_TIMESTAMP = booleanPreferencesKey("tracing.activation.timestamp")
        private val TEST_RESULT_NOTIFICATION_SENT = booleanPreferencesKey("test.notification.sent")
        private val LOWERED_RISK_LEVEL = booleanPreferencesKey("notification.risk.lowered")
        private val ADDITIONAL_HIGH_RISK_LEVEL = booleanPreferencesKey("notification.risk.additionalhigh")
        private val LAST_HIGH_RISK_LOCALDATE = longPreferencesKey("tracing.lasthighrisk.localdate")
        private val PKEY_SHOW_RISK_LEVEL_BADGE = booleanPreferencesKey("notifications.risk.level.change.badge")
    }
}
