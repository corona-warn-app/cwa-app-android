package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsRAKeySubmissionStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRTestResultSettings @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @AnalyticsTestResultSettingsDataStore private val dataStore: DataStore<Preferences>
) : AnalyticsTestResultSettings(mapper, sharedPrefKeySuffix, dataStore) {
    companion object {
        const val sharedPrefKeySuffix = "" // the original
    }
}

@Singleton
class AnalyticsRATestResultSettings @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @AnalyticsTestResultSettingsDataStore private val dataStore: DataStore<Preferences>
) : AnalyticsTestResultSettings(mapper, sharedPrefKeySuffix, dataStore) {
    companion object {
        const val sharedPrefKeySuffix = "_RAT"
    }
}

open class AnalyticsTestResultSettings(
    private val mapper: ObjectMapper,
    private val sharedPrefKeySuffix: String,
    private val dataStore: DataStore<Preferences>
) {

    val testRegisteredAt = dataStore.dataRecovering.distinctUntilChanged(
        key = longPreferencesKey(PREFS_KEY_TEST_REGISTERED_AT + sharedPrefKeySuffix), defaultValue = 0L
    ).map { value ->
        Timber.d(sharedPrefKeySuffix)
        if (value != 0L) {
            Instant.ofEpochMilli(value)
        } else null
    }

    suspend fun updateTestRegisteredAt(value: Instant) = dataStore.trySetValue(
        preferencesKey = longPreferencesKey(PREFS_KEY_TEST_REGISTERED_AT + sharedPrefKeySuffix),
        value = value.toEpochMilli()
    )

    val ewRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW + sharedPrefKeySuffix),
        defaultValue = PpaData.PPARiskLevel.RISK_LEVEL_LOW.number
    ).map { value ->
        PpaData.PPARiskLevel.forNumber(value)
    }

    suspend fun updateEwRiskLevelAtTestRegistration(value: PpaData.PPARiskLevel) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW + sharedPrefKeySuffix),
        value = value.number
    )

    val ptRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT + sharedPrefKeySuffix),
        defaultValue = PpaData.PPARiskLevel.RISK_LEVEL_LOW.number
    ).map { value ->
        PpaData.PPARiskLevel.forNumber(value)
    }

    suspend fun updatePtRiskLevelAtTestRegistration(value: PpaData.PPARiskLevel) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT + sharedPrefKeySuffix),
        value = value.number
    )

    val finalTestResultReceivedAt = dataStore.dataRecovering.distinctUntilChanged(
        key = longPreferencesKey(PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT + sharedPrefKeySuffix),
        defaultValue = 0L
    ).map { value ->
        if (value != 0L) {
            Instant.ofEpochMilli(value)
        } else null
    }

    suspend fun updateFinalTestResultReceivedAt(value: Instant) = dataStore.trySetValue(
        preferencesKey = longPreferencesKey(PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT + sharedPrefKeySuffix),
        value = value.toEpochMilli()
    )

    val testResult = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_TEST_RESULT + sharedPrefKeySuffix),
        defaultValue = -1
    ).map { value ->
        if (value == -1) {
            null
        } else {
            CoronaTestResult.fromInt(value)
        }
    }

    suspend fun updateTestResult(value: CoronaTestResult) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_TEST_RESULT + sharedPrefKeySuffix),
        value = value.value
    )

    val ewHoursSinceHighRiskWarningAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_HOURS_SINCE_WARNING_EW + sharedPrefKeySuffix),
        defaultValue = -1
    )

    suspend fun updateEwHoursSinceHighRiskWarningAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_HOURS_SINCE_WARNING_EW + sharedPrefKeySuffix),
        value = value
    )

    val ptHoursSinceHighRiskWarningAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_HOURS_SINCE_WARNING_PT + sharedPrefKeySuffix),
        defaultValue = -1
    )

    suspend fun updatePtHoursSinceHighRiskWarningAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_HOURS_SINCE_WARNING_PT + sharedPrefKeySuffix),
        value = value
    )

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_DAYS_SINCE_RISK_LEVEL_EW + sharedPrefKeySuffix),
        defaultValue = -1
    )

    suspend fun updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_DAYS_SINCE_RISK_LEVEL_EW + sharedPrefKeySuffix),
        value = value
    )

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(PREFS_KEY_DAYS_SINCE_RISK_LEVEL_PT + sharedPrefKeySuffix),
        defaultValue = -1
    )

    suspend fun updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(PREFS_KEY_DAYS_SINCE_RISK_LEVEL_PT + sharedPrefKeySuffix),
        value = value
    )

    val exposureWindowsAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = stringPreferencesKey(PREFS_KEY_EXPOSURE_WINDOWS_AT_REGISTRATION + sharedPrefKeySuffix),
        defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) {
            mapper.readValue<List<AnalyticsExposureWindow>>(value)
        } else {
            null
        }
    }

    suspend fun updateExposureWindowsAtTestRegistration(value: List<AnalyticsExposureWindow>) = dataStore.trySetValue(
        preferencesKey = stringPreferencesKey(PREFS_KEY_EXPOSURE_WINDOWS_AT_REGISTRATION + sharedPrefKeySuffix),
        value = mapper.writeValueAsString(value)
    )

    val exposureWindowsUntilTestResult = dataStore.dataRecovering.distinctUntilChanged(
        key = stringPreferencesKey(PREFS_KEY_EXPOSURE_WINDOWS_UNTIL_TEST_RESULT + sharedPrefKeySuffix),
        defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) {
            mapper.readValue<List<AnalyticsExposureWindow>>(value)
        } else {
            null
        }
    }

    suspend fun updateExposureWindowsUntilTestResult(value: List<AnalyticsExposureWindow>) = dataStore.trySetValue(
        preferencesKey = stringPreferencesKey(PREFS_KEY_EXPOSURE_WINDOWS_UNTIL_TEST_RESULT + sharedPrefKeySuffix),
        value = mapper.writeValueAsString(value)
    )

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        val keys = dataStore.data.first().asMap().keys.filter {
            if (sharedPrefKeySuffix.isEmpty()) {
                it.name.endsWith(AnalyticsRAKeySubmissionStorage.sharedPrefKeySuffix).not()
            } else {
                it.name.endsWith(AnalyticsRAKeySubmissionStorage.sharedPrefKeySuffix)
            }
        }
        dataStore.edit { prefs -> keys.forEach { key -> prefs.remove(key) } }
    }

    companion object {
        private const val PREFS_KEY_TEST_RESULT = "testResultDonor.testResultAtRegistration" // wrong name legacy

        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW = "testResultDonor.riskLevelAtRegistration"
        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT = "testResultDonor.ptRiskLevelAtRegistration"

        private const val PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT = "testResultDonor.finalTestResultReceivedAt"

        private const val PREFS_KEY_TEST_REGISTERED_AT = "testResultDonor.testRegisteredAt"

        private const val PREFS_KEY_HOURS_SINCE_WARNING_EW =
            "testResultDonor.ewHoursSinceHighRiskWarningAtTestRegistration"
        private const val PREFS_KEY_HOURS_SINCE_WARNING_PT =
            "testResultDonor.ptHoursSinceHighRiskWarningAtTestRegistration"

        private const val PREFS_KEY_DAYS_SINCE_RISK_LEVEL_EW =
            "testResultDonor.ewDaysSinceMostRecentDateAtPtRiskLevelAtTestRegistration"
        private const val PREFS_KEY_DAYS_SINCE_RISK_LEVEL_PT =
            "testResultDonor.ptDaysSinceMostRecentDateAtPtRiskLevelAtTestRegistration"

        private const val PREFS_KEY_EXPOSURE_WINDOWS_AT_REGISTRATION =
            "testResultDonor.exposureWindowsAtTestRegistration"

        private const val PREFS_KEY_EXPOSURE_WINDOWS_UNTIL_TEST_RESULT =
            "testResultDonor.exposureWindowsUntilTestResult"
    }
}

private val TAG = tag<AnalyticsTestResultSettings>()
