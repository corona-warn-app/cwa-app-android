package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate

/**
 * Maps Affected fields to [EvaluatedField]
 */
fun mapAffectedFields(affectedFields: List<String>, certificate: CwaCovidCertificate): List<EvaluatedField> {
    return affectedFields.mapNotNull { field ->
        val stringResource = field.stringResource
        if (stringResource != -1) EvaluatedField(stringResource, certificateValue(field, certificate)) else null
    }
}

/**
 * Returns string resource id if the field in the allowed list
 * `-1` otherwise
 */
@get:StringRes
private val String.stringResource: Int
    get() = when (this) {
        "v.0.tg" -> R.string.rule_disease_or_agent_targeted
        "v.0.vp" -> R.string.rule_vaccine
        "v.0.mp" -> R.string.rule_vaccine_type
        "v.0.ma" -> R.string.rule_vaccine_manufacturer
        "v.0.dn" -> R.string.rule_vaccination_number
        "v.0.sd" -> R.string.rule_vaccination_total_number
        "v.0.dt" -> R.string.rule_vaccination_date
        "v.0.co" -> R.string.rule_vaccination_country
        "v.0.is" -> R.string.rule_certificate_issuer
        "v.0.ci" -> R.string.rule_unique_certificate_identifier
        "t.0.tg" -> R.string.rule_certificate_agent_targeted
        "t.0.tt" -> R.string.rule_test_type
        "t.0.nm" -> R.string.rule_test_name
        "t.0.ma" -> R.string.rule_test_name
        "t.0.sc" -> R.string.rule_sample_collected_at
        "t.0.tr" -> R.string.rule_test_result
        "t.0.tc" -> R.string.rule_test_center
        "t.0.co" -> R.string.rule_test_country
        "t.0.is" -> R.string.rule_certificate_issuer
        "t.0.ci" -> R.string.rule_unique_certificate_identifier
        "r.0.tg" -> R.string.rule_certificate_agent_targeted
        "r.0.fr" -> R.string.rule_date_of_frist_positive_test_result
        "r.0.co" -> R.string.rule_test_country
        "r.0.is" -> R.string.rule_certificate_issuer
        "r.0.df" -> R.string.rule_certificate_valid_from
        "r.0.du" -> R.string.rule_certificate_valid_until
        "r.0.ci" -> R.string.rule_unique_certificate_identifier
        else -> -1
    }

private fun certificateValue(field: String, certificate: CwaCovidCertificate): String {
    return when (certificate) {
        is TestCertificate -> when (field) {
            "t.0.tg" -> ""
            "t.0.tt" -> ""
            "t.0.nm" -> ""
            "t.0.ma" -> ""
            "t.0.sc" -> ""
            "t.0.tr" -> ""
            "t.0.tc" -> ""
            "t.0.co" -> ""
            "t.0.is" -> ""
            "t.0.ci" -> ""
            else -> ""
        }
        is VaccinationCertificate -> when (field) {
            "v.0.tg" -> ""
            "v.0.vp" -> ""
            "v.0.mp" -> ""
            "v.0.ma" -> ""
            "v.0.dn" -> ""
            "v.0.sd" -> ""
            "v.0.dt" -> ""
            "v.0.co" -> ""
            "v.0.is" -> ""
            "v.0.ci" -> ""
            else -> ""
        }

        is RecoveryCertificate -> when (field) {
            "r.0.tg" -> ""
            "r.0.fr" -> ""
            "r.0.co" -> ""
            "r.0.is" -> ""
            "r.0.df" -> ""
            "r.0.du" -> ""
            "r.0.ci" -> ""
            else -> ""
        }
        else -> ""
    }
}

data class EvaluatedField(
    @StringRes val fieldResourceId: Int,
    val certificateFieldValue: String
)
