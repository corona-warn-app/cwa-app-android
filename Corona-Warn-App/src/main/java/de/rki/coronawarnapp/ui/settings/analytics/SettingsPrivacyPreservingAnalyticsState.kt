package de.rki.coronawarnapp.ui.settings.analytics

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData

data class SettingsPrivacyPreservingAnalyticsState(
    val isAnalyticsEnabled: Boolean,
    val ageGroup: PpaData.PPAAgeGroup,
    val federalState: PpaData.PPAFederalState,
    val district: Districts.District?
) {
    fun getAgeGroupRowBodyText(context: Context) =
        context.getString(ageGroup.labelStringRes)

    fun isDistrictRowVisible() =
        federalState != PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED

    fun getDistrictRowBodyText(context: Context) =
        district?.districtName ?: context.getString(R.string.analytics_userinput_district_unspecified)

    fun getFederalStateRowBodyText(context: Context) =
        context.getString(federalState.labelStringRes)

    fun isSettingsPpaSwitchOn() =
        isAnalyticsEnabled

    fun getSettingsPpaSwitchRowStateText(context: Context) =
        when (isAnalyticsEnabled) {
            true -> context.getString(R.string.settings_on)
            false -> context.getString(R.string.settings_off)
        }
}
