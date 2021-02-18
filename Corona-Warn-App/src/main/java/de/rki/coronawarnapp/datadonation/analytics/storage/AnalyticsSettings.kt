package de.rki.coronawarnapp.datadonation.analytics.storage

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.ExposureRiskMetadata
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE)
    }

    val previousExposureRiskMetadata = prefs.createFlowPreference(
        key = PREVIOUS_EXPOSURE_RISK_METADATA,
        reader = { key ->
            getString(key, null)?.let { prefString ->
                prefString.decodeBase64()?.toByteArray()?.let {
                    try {
                        ExposureRiskMetadata.parseFrom(it)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        },
        writer = { key, value ->
            putString(key, value?.toByteArray()?.toByteString()?.base64())
        }
    )

    val userInfoAgeGroup = prefs.createFlowPreference(
        key = PKEY_USERINFO_AGEGROUP,
        reader = { key ->
            PpaData.PPAAgeGroup.forNumber(getInt(key, 0)) ?: PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        },
        writer = { key, value ->
            val numberToWrite = when (value) {
                PpaData.PPAAgeGroup.UNRECOGNIZED -> PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED.number
                else -> value.number
            }
            putInt(key, numberToWrite)
        }
    )

    val userInfoFederalState = prefs.createFlowPreference(
        key = PKEY_USERINFO_FEDERALSTATE,
        reader = { key ->
            PpaData.PPAFederalState.forNumber(getInt(key, -1)) ?: PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        },
        writer = { key, value ->
            val numberToWrite = when (value) {
                PpaData.PPAFederalState.UNRECOGNIZED -> PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED.number
                else -> value.number
            }
            putInt(key, numberToWrite)
        }
    )

    val userInfoDistrict = prefs.createFlowPreference(
        key = PKEY_USERINFO_DISTRICT,
        defaultValue = 0
    )

    val lastSubmittedTimestamp = prefs.createFlowPreference(
        key = PKEY_LAST_SUBMITTED_TIMESTAMP,
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

    val analyticsEnabled = prefs.createFlowPreference(
        key = PKEY_ANALYTICS_ENABLED,
        defaultValue = false
    )

    val testScannedAfterConsent = prefs.createFlowPreference(
        key = PREFS_KEY_TEST_SCANNED_AFTER_CONSENT,
        defaultValue = false
    )

    val riskLevelAtTestRegistration = prefs.createFlowPreference(
        key = PREFS_KEY_RISK_LEVEL_AT_REGISTRATION,
        reader = { key ->
            PpaData.PPARiskLevel.forNumber(getInt(key, PpaData.PPARiskLevel.RISK_LEVEL_LOW.number))
                ?: PpaData.PPARiskLevel.RISK_LEVEL_LOW
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val finalTestResultReceivedAt = prefs.createFlowPreference(
        key = PREFS_KEY_FINAL_TEST_RESULT_AT,
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

    companion object {
        private const val PREVIOUS_EXPOSURE_RISK_METADATA = "exposurerisk.metadata.previous"
        private const val PKEY_USERINFO_AGEGROUP = "userinfo.agegroup"
        private const val PKEY_USERINFO_FEDERALSTATE = "userinfo.federalstate"
        private const val PKEY_USERINFO_DISTRICT = "userinfo.district"
        private const val PKEY_LAST_SUBMITTED_TIMESTAMP = "analytics.submission.timestamp"
        private const val PKEY_ANALYTICS_ENABLED = "analytics.enabled"
        private const val PREFS_KEY_TEST_SCANNED_AFTER_CONSENT = "analytics.testScannedAfterConsent"
        private const val PREFS_KEY_RISK_LEVEL_AT_REGISTRATION = "analytics.riskLevelAtRegistration"
        private const val PREFS_KEY_FINAL_TEST_RESULT_AT = "analytics.finalTestResultReceivedAt"
    }
}
