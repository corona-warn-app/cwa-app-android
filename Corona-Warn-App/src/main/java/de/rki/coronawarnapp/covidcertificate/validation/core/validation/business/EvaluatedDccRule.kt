package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import kotlinx.parcelize.Parcelize

@Parcelize
data class EvaluatedDccRule(
    val rule: DccValidationRule,
    val result: DccValidationRule.Result,
) : Parcelable
