package de.rki.coronawarnapp.datadonation.analytics.storage

import android.content.Context
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestResultDonorSettings @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_testResultDonor", Context.MODE_PRIVATE)
    }

    val testScannedAfterConsent = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_SCANNED_AFTER_CONSENT,
        defaultValue = false
    )

    val ewRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_EW_RISK_LEVEL_AT_REGISTRATION,
        reader = { key ->
            PpaData.PPARiskLevel.forNumber(getInt(key, PpaData.PPARiskLevel.RISK_LEVEL_LOW.number))
                ?: PpaData.PPARiskLevel.RISK_LEVEL_LOW
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val ptRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_PT_RISK_LEVEL_AT_REGISTRATION,
        reader = { key ->
            PpaData.PPARiskLevel.forNumber(getInt(key, PpaData.PPARiskLevel.RISK_LEVEL_LOW.number))
                ?: PpaData.PPARiskLevel.RISK_LEVEL_LOW
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val finalTestResultReceivedAt = prefs.createFlowPreference(
        key = PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT,
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
        key = PREFS_KEY_TEST_RESULT_AT_REGISTRATION,
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

    val ewMostRecentDateWithHighOrLowRiskLevel = prefs.createFlowPreference(
        key = PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_EW_RISK_LEVEL,
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
        key = PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_PT_RISK_LEVEL,
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

    val riskLevelTurnedRedTime = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME,
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

    fun saveTestResultDonorDataAtRegistration(
        testResult: CoronaTestResult,
        lastEwRiskResult: EwRiskLevelResult,
        lastPtRiskResult: PtRiskLevelResult,
    ) {
        testScannedAfterConsent.update { true }
        testResultAtRegistration.update { testResult }
        if (testResult in listOf(CoronaTestResult.PCR_POSITIVE, CoronaTestResult.PCR_NEGATIVE)) {
            finalTestResultReceivedAt.update { timeStamper.nowUTC }
        }

        ewRiskLevelAtTestRegistration.update { lastEwRiskResult.toMetadataRiskLevel() }
        ptRiskLevelAtTestRegistration.update { lastPtRiskResult.toMetadataRiskLevel() }
    }

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val PREFS_KEY_TEST_SCANNED_AFTER_CONSENT = "testResultDonor.testScannedAfterConsent"
        private const val PREFS_KEY_TEST_RESULT_AT_REGISTRATION = "testResultDonor.testResultAtRegistration"
        private const val PREFS_KEY_EW_RISK_LEVEL_AT_REGISTRATION = "testResultDonor.riskLevelAtRegistration"
        private const val PREFS_KEY_PT_RISK_LEVEL_AT_REGISTRATION = "testResultDonor.ptRiskLevelAtRegistration"
        private const val PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT = "testResultDonor.finalTestResultReceivedAt"
        private const val PREFS_KEY_RISK_LEVEL_TURNED_RED_TIME = "testResultDonor.riskLevelTurnedRedTime"
        private const val PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_EW_RISK_LEVEL =
            "testResultDonor.mostRecentWithHighOrLowRiskLevel"
        private const val PREFS_KEY_MOST_RECENT_WITH_HIGH_OR_LOW_PT_RISK_LEVEL =
            "testResultDonor.mostRecentWithHighOrLowPtRiskLevel"
    }
}
