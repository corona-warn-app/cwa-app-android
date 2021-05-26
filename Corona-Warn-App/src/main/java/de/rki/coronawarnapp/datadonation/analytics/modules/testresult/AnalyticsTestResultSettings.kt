package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.common.isFinal
import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRTestResultSettings @Inject constructor(
    @AppContext context: Context,
    timeStamper: TimeStamper
) : AnalyticsTestResultSettings(context, timeStamper, "") // the original

@Singleton
class AnalyticsRATestResultSettings @Inject constructor(
    @AppContext context: Context,
    timeStamper: TimeStamper
) : AnalyticsTestResultSettings(context, timeStamper, "_RAT")

abstract class AnalyticsTestResultSettings(
    private val context: Context,
    private val timeStamper: TimeStamper,
    sharedPrefKeySuffix: String
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_testResultDonor$sharedPrefKeySuffix", Context.MODE_PRIVATE)
    }

    val testRegisteredAt = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_REGISTERED_AT + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val testScannedAfterConsent = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_SCANNED_AFTER_CONSENT + sharedPrefKeySuffix,
        defaultValue = false
    )

    val ewRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW + sharedPrefKeySuffix,
        reader = { key ->
            PpaData.PPARiskLevel.forNumber(getInt(key, PpaData.PPARiskLevel.RISK_LEVEL_LOW.number))
                ?: PpaData.PPARiskLevel.RISK_LEVEL_LOW
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val ptRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT + sharedPrefKeySuffix,
        reader = { key ->
            PpaData.PPARiskLevel.forNumber(getInt(key, PpaData.PPARiskLevel.RISK_LEVEL_LOW.number))
                ?: PpaData.PPARiskLevel.RISK_LEVEL_LOW
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val finalTestResultReceivedAt = prefs.createFlowPreference(
        key = PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val testResultAtRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_RESULT_AT_REGISTRATION + sharedPrefKeySuffix,
        reader = { key ->
            val value = getInt(key, -1)
            if (value == -1) {
                null
            } else {
                CoronaTestResult.fromInt(value)
            }
        },
        writer = { key, result ->
            putInt(key, result?.value ?: -1)
        }
    )
    val ewHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_HOURS_SINCE_WARNING_EW + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ptHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_HOURS_SINCE_WARNING_PT + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_DAYS_SINCE_RISK_LEVEL_EW + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_DAYS_SINCE_RISK_LEVEL_PT + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ewMostRecentDateWithHighOrLowRiskLevel = prefs.createFlowPreference(
        key = PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_RISK_LEVEL_EW + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val ptMostRecentDateWithHighOrLowRiskLevel = prefs.createFlowPreference(
        key = PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_RISK_LEVEL_PT + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val ewRiskLevelTurnedRedTime = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME_EW + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val ptRiskLevelTurnedRedTime = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME_PT + sharedPrefKeySuffix,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    fun saveTestResultDonorDataAtRegistration(testResult: CoronaTestResult, lastResult: CombinedEwPtRiskLevelResult) {
        testScannedAfterConsent.update { true }
        testResultAtRegistration.update { testResult }
        if (testResult.isFinal) {
            finalTestResultReceivedAt.update { timeStamper.nowUTC }
        }

        ewRiskLevelAtTestRegistration.update { lastResult.ewRiskLevelResult.toMetadataRiskLevel() }
        ptRiskLevelAtTestRegistration.update { lastResult.ptRiskLevelResult.riskState.toMetadataRiskLevel() }
    }

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val PREFS_KEY_TEST_SCANNED_AFTER_CONSENT = "testResultDonor.testScannedAfterConsent"
        private const val PREFS_KEY_TEST_RESULT_AT_REGISTRATION = "testResultDonor.testResultAtRegistration"

        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW = "testResultDonor.riskLevelAtRegistration"
        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT = "testResultDonor.ptRiskLevelAtRegistration"

        private const val PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT = "testResultDonor.finalTestResultReceivedAt"

        private const val PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME_EW = "testResultDonor.riskLevelTurnedRedTime"
        private const val PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME_PT = "testResultDonor.ptRiskLevelTurnedRedTime"

        private const val PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_RISK_LEVEL_EW =
            "testResultDonor.mostRecentWithHighOrLowRiskLevel"
        private const val PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_RISK_LEVEL_PT =
            "testResultDonor.ptMostRecentWithHighOrLowRiskLevel"

        private const val PREFS_KEY_TEST_REGISTERED_AT =
            "testResultDonor.testRegisteredAt"

        private const val PREFS_KEY_HOURS_SINCE_WARNING_EW =
            "testResultDonor.ewHoursSinceHighRiskWarningAtTestRegistration"
        private const val PREFS_KEY_HOURS_SINCE_WARNING_PT =
            "testResultDonor.ptHoursSinceHighRiskWarningAtTestRegistration"

        private const val PREFS_KEY_DAYS_SINCE_RISK_LEVEL_EW =
            "testResultDonor.ewDaysSinceMostRecentDateAtPtRiskLevelAtTestRegistration"
        private const val PREFS_KEY_DAYS_SINCE_RISK_LEVEL_PT =
            "testResultDonor.ptDaysSinceMostRecentDateAtPtRiskLevelAtTestRegistration"
    }
}
