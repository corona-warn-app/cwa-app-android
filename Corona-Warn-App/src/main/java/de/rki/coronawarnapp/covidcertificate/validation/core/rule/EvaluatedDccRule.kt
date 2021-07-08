package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EvaluatedDccRule(
    val rule: DccValidationRule,
    val result: DccValidationRule.Result,
) : Parcelable
