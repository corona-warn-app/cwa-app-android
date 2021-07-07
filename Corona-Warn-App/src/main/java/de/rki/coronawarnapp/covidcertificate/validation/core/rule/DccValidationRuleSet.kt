package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccValidationRuleSet(
    val countryCode: String,
    val type: DccValidationRule.Type,
    val eTag: String,
    val rules: List<DccValidationRule>
) : Parcelable
