package de.rki.coronawarnapp.datadonation.analytics.common

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData

val PpaData.PPAAgeGroup.labelStringRes: Int
    @StringRes
    get() = when (this) {
        PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED -> R.string.analytics_userinput_agegroup_unspecified
        PpaData.PPAAgeGroup.AGE_GROUP_0_TO_29 -> R.string.analytics_userinput_agegroup_0_to_29
        PpaData.PPAAgeGroup.AGE_GROUP_30_TO_59 -> R.string.analytics_userinput_agegroup_30_to_59
        PpaData.PPAAgeGroup.AGE_GROUP_FROM_60 -> R.string.analytics_userinput_agegroup_from_60
        PpaData.PPAAgeGroup.UNRECOGNIZED -> throw UnsupportedOperationException(
            "PpaData.PPAAgeGroup.UNRECOGNIZED has no label."
        )
    }

val PpaData.PPAFederalState.labelStringRes: Int
    @StringRes
    get() = when (this) {
        PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED -> R.string.analytics_userinput_federalstate_unspecified
        PpaData.PPAFederalState.FEDERAL_STATE_BW -> R.string.analytics_userinput_federalstate_bw
        PpaData.PPAFederalState.FEDERAL_STATE_BY -> R.string.analytics_userinput_federalstate_by
        PpaData.PPAFederalState.FEDERAL_STATE_BE -> R.string.analytics_userinput_federalstate_be
        PpaData.PPAFederalState.FEDERAL_STATE_BB -> R.string.analytics_userinput_federalstate_bb
        PpaData.PPAFederalState.FEDERAL_STATE_HB -> R.string.analytics_userinput_federalstate_hb
        PpaData.PPAFederalState.FEDERAL_STATE_HH -> R.string.analytics_userinput_federalstate_hh
        PpaData.PPAFederalState.FEDERAL_STATE_HE -> R.string.analytics_userinput_federalstate_he
        PpaData.PPAFederalState.FEDERAL_STATE_MV -> R.string.analytics_userinput_federalstate_mv
        PpaData.PPAFederalState.FEDERAL_STATE_NI -> R.string.analytics_userinput_federalstate_ni
        PpaData.PPAFederalState.FEDERAL_STATE_NRW -> R.string.analytics_userinput_federalstate_nrw
        PpaData.PPAFederalState.FEDERAL_STATE_RP -> R.string.analytics_userinput_federalstate_rp
        PpaData.PPAFederalState.FEDERAL_STATE_SL -> R.string.analytics_userinput_federalstate_sl
        PpaData.PPAFederalState.FEDERAL_STATE_SN -> R.string.analytics_userinput_federalstate_sn
        PpaData.PPAFederalState.FEDERAL_STATE_ST -> R.string.analytics_userinput_federalstate_st
        PpaData.PPAFederalState.FEDERAL_STATE_SH -> R.string.analytics_userinput_federalstate_sh
        PpaData.PPAFederalState.FEDERAL_STATE_TH -> R.string.analytics_userinput_federalstate_th
        PpaData.PPAFederalState.UNRECOGNIZED -> throw UnsupportedOperationException(
            "PpaData.PPAFederalState.UNRECOGNIZED has no label"
        )
    }

val PpaData.PPAFederalState.federalStateShortName: String
    get() = when (this) {
        PpaData.PPAFederalState.FEDERAL_STATE_BW -> "BW"
        PpaData.PPAFederalState.FEDERAL_STATE_BY -> "BY"
        PpaData.PPAFederalState.FEDERAL_STATE_BE -> "BE"
        PpaData.PPAFederalState.FEDERAL_STATE_BB -> "BB"
        PpaData.PPAFederalState.FEDERAL_STATE_HB -> "HB"
        PpaData.PPAFederalState.FEDERAL_STATE_HH -> "HH"
        PpaData.PPAFederalState.FEDERAL_STATE_HE -> "HE"
        PpaData.PPAFederalState.FEDERAL_STATE_MV -> "MV"
        PpaData.PPAFederalState.FEDERAL_STATE_NI -> "NI"
        PpaData.PPAFederalState.FEDERAL_STATE_NRW -> "NW"
        PpaData.PPAFederalState.FEDERAL_STATE_RP -> "RP"
        PpaData.PPAFederalState.FEDERAL_STATE_SL -> "SL"
        PpaData.PPAFederalState.FEDERAL_STATE_SN -> "SN"
        PpaData.PPAFederalState.FEDERAL_STATE_ST -> "ST"
        PpaData.PPAFederalState.FEDERAL_STATE_SH -> "SH"
        PpaData.PPAFederalState.FEDERAL_STATE_TH -> "TH"
        PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED -> ""
        PpaData.PPAFederalState.UNRECOGNIZED -> throw UnsupportedOperationException(
            "PpaData.PPAFederalState.UNRECOGNIZED has no short name"
        )
    }

fun RiskState.toMetadataRiskLevel(): PpaData.PPARiskLevel =
    when (this) {
        RiskState.INCREASED_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        RiskState.LOW_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_LOW
        else -> PpaData.PPARiskLevel.RISK_LEVEL_UNKNOWN
    }
