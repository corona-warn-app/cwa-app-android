package de.rki.coronawarnapp.datadonation.analytics

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AppContext
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

    val userInfoAgeGroup = prefs.createFlowPreference(
        key = PKEY_USERINFO_AGEGROUP,
        reader = { key ->
            PpaData.PPAAgeGroup.forNumber(getInt(key, 0)) ?: PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val userInfoFederalState = prefs.createFlowPreference(
        key = PKEY_USERINFO_FEDERALSTATE,
        reader = { key ->
            PpaData.PPAFederalState.forNumber(getInt(key, -1)) ?: PpaData.PPAFederalState.UNRECOGNIZED
        },
        writer = { key, value ->
            putInt(key, value.number)
        }
    )

    val userInfoDistrict = prefs.createFlowPreference(
        key = PKEY_USERINFO_DISTRICT,
        defaultValue = 0
    )

    companion object {
        private const val PKEY_USERINFO_AGEGROUP = "userinfo.agegroup"
        private const val PKEY_USERINFO_FEDERALSTATE = "userinfo.federalstate"
        private const val PKEY_USERINFO_DISTRICT = "userinfo.district"
    }
}
