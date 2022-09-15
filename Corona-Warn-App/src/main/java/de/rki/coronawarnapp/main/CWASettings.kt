package de.rki.coronawarnapp.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.appconfig.ConfigData.DeviceTimeState
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * For general app related values,
 * e.g. "Has dialog been shown", as "OnBoarding been shown?"
 */
class CWASettings @Inject constructor(
    @CwaSettingsDataStore val dataStore: DataStore<Preferences>
) : Resettable {

    var wasDeviceTimeIncorrectAcknowledged = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_DEVICE_TIME_INCORRECT_ACK, defaultValue = false
    )

    suspend fun updateWasDeviceTimeIncorrectAcknowledged(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_DEVICE_TIME_INCORRECT_ACK, value = value
    )

    var wasTracingExplanationDialogShown = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_TRACING_DIALOG_SHOWN, defaultValue = false
    )

    suspend fun updateWasTracingExplanationDialogShown(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_TRACING_DIALOG_SHOWN, value = value
    )

    var wasInteroperabilityShownAtLeastOnce = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE, defaultValue = false
    )

    suspend fun updateWasInteroperabilityShownAtLeastOnce(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE, value = value
    )

    var wasCertificateGroupingMigrationAcknowledged = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_CERT_GROUPING_MIGRATION, defaultValue = false
    )

    suspend fun updateWasCertificateGroupingMigrationAcknowledged(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_CERT_GROUPING_MIGRATION, value = value
    )

    var firstReliableDeviceTime: Flow<Instant?> = dataStore.dataRecovering.map(PKEY_DEVICE_TIME_FIRST_RELIABLE)
        .map { if (it != null && it != 0L) Instant.ofEpochMilli(it) else null }.distinctUntilChanged()

    suspend fun updateFirstReliableDeviceTime(value: Instant) = dataStore.trySetValue(
        preferencesKey = PKEY_DEVICE_TIME_FIRST_RELIABLE, value = value.toEpochMilli()
    )

    var lastDeviceTimeStateChangeAt: Flow<Instant?> =
        dataStore.dataRecovering.map(PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME)
            .map { if (it != null && it != 0L) Instant.ofEpochMilli(it) else null }.distinctUntilChanged()

    suspend fun updateLastDeviceTimeStateChangeAt(value: Instant) = dataStore.trySetValue(
        preferencesKey = PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME, value = value.toEpochMilli()
    )

    var lastDeviceTimeStateChangeState: Flow<DeviceTimeState> =
        dataStore.dataRecovering.map(PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE)
            .map { value -> DeviceTimeState.values().single { it.key == value } }.distinctUntilChanged()

    suspend fun updateLastDeviceTimeStateChangeState(value: DeviceTimeState) = dataStore.trySetValue(
        preferencesKey = PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE, value = value.key
    )

    var numberOfRemainingSharePositiveTestResultRemindersPcr: Flow<Int> = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR, defaultValue = Int.MIN_VALUE
    )

    suspend fun updateNumberOfRemainingSharePositiveTestResultRemindersPcr(value: Int) = dataStore.trySetValue(
        preferencesKey = PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR, value = value
    )

    var idOfPositiveTestResultRemindersPcr: Flow<TestIdentifier> = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_PCR, defaultValue = String()
    )

    suspend fun updateIdOfPositiveTestResultRemindersPcr(value: TestIdentifier) = dataStore.trySetValue(
        preferencesKey = PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_PCR, value = value
    )

    var numberOfRemainingSharePositiveTestResultRemindersRat = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT, defaultValue = Int.MIN_VALUE
    )

    suspend fun updateNumberOfRemainingSharePositiveTestResultRemindersRat(value: Int) = dataStore.trySetValue(
        preferencesKey = PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT, value = value
    )

    var idOfPositiveTestResultRemindersRat: Flow<TestIdentifier> = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_RAT, defaultValue = String()
    )

    suspend fun updateIdOfPositiveTestResultRemindersRat(value: TestIdentifier) = dataStore.trySetValue(
        preferencesKey = PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_RAT, value = value
    )

    val lastChangelogVersion = dataStore.dataRecovering.distinctUntilChanged(
        key = LAST_CHANGELOG_VERSION, defaultValue = DEFAULT_APP_VERSION
    )

    suspend fun updateLastChangelogVersion(value: Long) = dataStore.trySetValue(
        preferencesKey = LAST_CHANGELOG_VERSION, value = value
    )

    val lastNotificationsOnboardingVersionCode = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_NOTIFICATIONS_ONBOARDED_VERSION_CODE, defaultValue = 0L
    )

    suspend fun updateLastNotificationsOnboardingVersionCode(value: Long) = dataStore.trySetValue(
        preferencesKey = PKEY_NOTIFICATIONS_ONBOARDED_VERSION_CODE, value = value
    )

    val lastSuppressRootInfoVersionCode = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_SUPPRESS_ROOT_INFO_FOR_VERSION_CODE, defaultValue = 0L
    )

    suspend fun updateLastSuppressRootInfoVersionCode(value: Long) = dataStore.trySetValue(
        preferencesKey = PKEY_SUPPRESS_ROOT_INFO_FOR_VERSION_CODE, value = value
    )

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        private val PKEY_DEVICE_TIME_INCORRECT_ACK = booleanPreferencesKey("devicetime.incorrect.acknowledged")
        private val PKEY_TRACING_DIALOG_SHOWN = booleanPreferencesKey("tracing.dialog.shown")
        private val PKEY_INTEROPERABILITY_SHOWED_AT_LEAST_ONCE = booleanPreferencesKey("interoperability.showed")
        private val PKEY_DEVICE_TIME_FIRST_RELIABLE = longPreferencesKey("devicetime.correct.first")
        private val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME = longPreferencesKey("devicetime.laststatechange.timestamp")
        private val PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE = stringPreferencesKey("devicetime.laststatechange.state")
        private val PKEY_CERT_GROUPING_MIGRATION = booleanPreferencesKey("device.grouping.migration")

        private val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_PCR = intPreferencesKey("testresults.count")
        private val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT_RAT = intPreferencesKey("testresults.count.rat")

        private val PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_PCR = stringPreferencesKey("testresults.id")
        private val PKEY_POSITIVE_TEST_RESULT_REMINDER_ID_RAT = stringPreferencesKey("testresults.id.rat")

        private val LAST_CHANGELOG_VERSION = longPreferencesKey("update.changelog.lastversion")
        const val DEFAULT_APP_VERSION = 1L

        private val PKEY_NOTIFICATIONS_ONBOARDED_VERSION_CODE =
            longPreferencesKey("notifications.onboarding.versionCode")
        private val PKEY_SUPPRESS_ROOT_INFO_FOR_VERSION_CODE = longPreferencesKey("suppress.root.info.versionCode")
    }
}
