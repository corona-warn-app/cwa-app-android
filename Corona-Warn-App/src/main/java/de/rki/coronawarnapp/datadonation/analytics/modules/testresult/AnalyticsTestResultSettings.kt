package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRTestResultSettings @Inject constructor(
    @AppContext context: Context,
    @BaseGson val gson: Gson,
) : AnalyticsTestResultSettings(context, gson, "") // the original

@Singleton
class AnalyticsRATestResultSettings @Inject constructor(
    @AppContext context: Context,
    @BaseGson val gson: Gson,
) : AnalyticsTestResultSettings(context, gson, "_RAT")

open class AnalyticsTestResultSettings(
    private val context: Context,
    gson: Gson,
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
            putLong(key, value?.toEpochMilli() ?: 0L)
        }
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
            putLong(key, value?.toEpochMilli() ?: 0L)
        }
    )

    val testResult = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_RESULT + sharedPrefKeySuffix,
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

    val exposureWindowsAtTestRegistration: FlowPreference<List<AnalyticsExposureWindow>?> = prefs.createFlowPreference(
        key = PREFS_KEY_EXPOSURE_WINDOWS_AT_REGISTRATION + sharedPrefKeySuffix,
        reader = FlowPreference.gsonReader<List<AnalyticsExposureWindow>?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    val exposureWindowsUntilTestResult: FlowPreference<List<AnalyticsExposureWindow>?> = prefs.createFlowPreference(
        key = PREFS_KEY_EXPOSURE_WINDOWS_UNTIL_TEST_RESULT + sharedPrefKeySuffix,
        reader = FlowPreference.gsonReader<List<AnalyticsExposureWindow>?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val PREFS_KEY_TEST_RESULT = "testResultDonor.testResultAtRegistration" // wrong name legacy

        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_EW = "testResultDonor.riskLevelAtRegistration"
        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION_PT = "testResultDonor.ptRiskLevelAtRegistration"

        private const val PREFS_KEY_FINAL_TEST_RESULT_RECEIVED_AT = "testResultDonor.finalTestResultReceivedAt"

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

        private const val PREFS_KEY_EXPOSURE_WINDOWS_AT_REGISTRATION =
            "testResultDonor.exposureWindowsAtTestRegistration"

        private const val PREFS_KEY_EXPOSURE_WINDOWS_UNTIL_TEST_RESULT =
            "testResultDonor.exposureWindowsUntilTestResult"
    }
}
