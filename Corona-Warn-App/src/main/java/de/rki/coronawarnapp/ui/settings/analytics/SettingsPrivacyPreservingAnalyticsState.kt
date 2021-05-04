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
    val isAgeGroupVisible: Boolean
        get() = isAnalyticsEnabled

    fun getAgeGroupRowBodyText(context: Context) =
        context.getString(ageGroup.labelStringRes)

    val isDistrictRowVisible
        get() = federalState != PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED && isAnalyticsEnabled

    fun getDistrictRowBodyText(context: Context) =
        district?.districtName ?: context.getString(R.string.analytics_userinput_district_unspecified)

    val isFederalStateRowVisible: Boolean
        get() = isAnalyticsEnabled

    fun getFederalStateRowBodyText(context: Context) =
        context.getString(federalState.labelStringRes)
}
