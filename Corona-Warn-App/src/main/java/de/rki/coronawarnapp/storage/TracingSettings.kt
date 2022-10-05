package de.rki.coronawarnapp.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.getValueOrDefault
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TracingSettings @Inject constructor(
    @TracingSettingsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    suspend fun isConsentGiven() = dataStore.getValueOrDefault(TRACING_ACTIVATION_TIMESTAMP, false)

    suspend fun updateConsentGiven(isConsentGiven: Boolean) = dataStore.trySetValue(
        preferencesKey = TRACING_ACTIVATION_TIMESTAMP,
        value = isConsentGiven
    )

    //region needed for migration ONLY. Use CoronaTestRepository
    suspend fun isTestResultAvailableNotificationSentMigration() = dataStore.getValueOrDefault(
        preferencesKey = TEST_RESULT_NOTIFICATION_SENT,
        defaultValue = false
    )

    suspend fun updateTestResultAvailableNotificationSentMigration(sent: Boolean) = dataStore.trySetValue(
        preferencesKey = TEST_RESULT_NOTIFICATION_SENT,
        value = sent
    )
    //endregion needed for migration ONLY.

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
        value = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: 0L
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

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        @VisibleForTesting
        val TRACING_ACTIVATION_TIMESTAMP = booleanPreferencesKey("tracing.activation.timestamp")

        @VisibleForTesting
        val TEST_RESULT_NOTIFICATION_SENT = booleanPreferencesKey("test.notification.sent")

        @VisibleForTesting
        val LOWERED_RISK_LEVEL = booleanPreferencesKey("notification.risk.lowered")

        @VisibleForTesting
        val ADDITIONAL_HIGH_RISK_LEVEL = booleanPreferencesKey("notification.risk.additionalhigh")

        @VisibleForTesting
        val LAST_HIGH_RISK_LOCALDATE = longPreferencesKey("tracing.lasthighrisk.localdate")

        @VisibleForTesting
        val PKEY_SHOW_RISK_LEVEL_BADGE = booleanPreferencesKey("notifications.risk.level.change.badge")
    }
}
