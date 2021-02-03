package de.rki.coronawarnapp.datadonation.analytics

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.ExposureRiskMetadata
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE)
    }

    val previousExposureRiskMetadata = FlowPreference(
        preferences = prefs,
        key = PREVIOUS_EXPOSURE_RISK_METADATA,
        reader = { key ->
            getString(key, null)?.let {
                ExposureRiskMetadata.parseFrom(ByteString.copyFromUtf8(it))
            }
        },
        writer = { key, value ->
            putString(key, value?.toByteString()?.toStringUtf8())
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

    companion object {
        private const val PREVIOUS_EXPOSURE_RISK_METADATA = "exposurerisk.metadata.previous"
        private const val PKEY_USERINFO_AGEGROUP = "userinfo.agegroup"
        private const val PKEY_USERINFO_FEDERALSTATE = "userinfo.federalstate"
        private const val PKEY_USERINFO_DISTRICT = "userinfo.district"
    }
}
