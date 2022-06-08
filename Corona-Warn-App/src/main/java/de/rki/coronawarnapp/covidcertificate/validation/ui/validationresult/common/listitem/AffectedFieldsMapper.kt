package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.lists.HasStableId

/**
 * Maps Affected fields to [EvaluatedField]
 */
fun mapAffectedFields(affectedFields: List<String>, certificate: CwaCovidCertificate): List<EvaluatedField> {
    return affectedFields.mapNotNull { field ->
        val stringResource = field.stringResource
        if (stringResource != -1) EvaluatedField(field, stringResource, certificateValue(field, certificate)) else null
    }
}

/**
 * Returns string resource id if the field in the allowed list
 * `-1` otherwise
 */
@get:StringRes
private val String.stringResource: Int
    get() = when (this) {
        // Vaccination certificate
        "v.0.vp" -> R.string.rule_vaccine_type
        "v.0.mp" -> R.string.rule_vaccine
        "v.0.ma" -> R.string.rule_vaccine_manufacturer
        "v.0.dn" -> R.string.rule_vaccination_number
        "v.0.sd" -> R.string.rule_vaccination_total_number
        "v.0.dt" -> R.string.rule_vaccination_date
        "v.0.co" -> R.string.rule_vaccination_country

        // Test certificate
        "t.0.tt" -> R.string.rule_test_type
        "t.0.nm" -> R.string.rule_test_name
        "t.0.ma" -> R.string.rule_test_manufacturer
        "t.0.sc" -> R.string.rule_sample_collected_at
        "t.0.tr" -> R.string.rule_test_result
        "t.0.tc" -> R.string.rule_test_center

        // Recovery certificate
        "r.0.fr" -> R.string.rule_date_of_frist_positive_test_result
        "r.0.df" -> R.string.rule_certificate_valid_from
        "r.0.du" -> R.string.rule_certificate_valid_until

        // Common fields labels
        // Certificate issuer
        "v.0.is",
        "r.0.is",
        "t.0.is" -> R.string.rule_certificate_issuer

        // Certificate id
        "t.0.ci",
        "v.0.ci",
        "r.0.ci" -> R.string.rule_certificate_unique_identifier

        // Targeted Agent / Disease
        "v.0.tg",
        "t.0.tg",
        "r.0.tg" -> R.string.rule_certificate_agent_targeted

        // Test country
        "t.0.co",
        "r.0.co" -> R.string.rule_test_country
        else -> -1
    }

private fun certificateValue(field: String, certificate: CwaCovidCertificate): String? {
    return when (certificate) {
        is VaccinationCertificate -> when (field) {
            "v.0.tg" -> certificate.targetDisease
            "v.0.vp" -> certificate.vaccineTypeName
            "v.0.mp" -> certificate.medicalProductName
            "v.0.ma" -> certificate.vaccineManufacturer
            "v.0.dn" -> certificate.doseNumber.toString()
            "v.0.sd" -> certificate.totalSeriesOfDoses.toString()
            "v.0.dt" -> certificate.vaccinatedOnFormatted
            "v.0.co" -> certificate.certificateCountry
            "v.0.is" -> certificate.certificateIssuer
            "v.0.ci" -> certificate.uniqueCertificateIdentifier
            else -> null
        }

        is TestCertificate -> when (field) {
            "t.0.tg" -> certificate.targetDisease
            "t.0.tt" -> certificate.testType
            "t.0.nm" -> certificate.testName
            "t.0.ma" -> certificate.testNameAndManufacturer
            "t.0.sc" -> certificate.sampleCollectedAtFormatted
            "t.0.tr" -> certificate.testResult
            "t.0.tc" -> certificate.testCenter
            "t.0.co" -> certificate.certificateCountry
            "t.0.is" -> certificate.certificateIssuer
            "t.0.ci" -> certificate.uniqueCertificateIdentifier
            else -> null
        }

        is RecoveryCertificate -> when (field) {
            "r.0.tg" -> certificate.targetDisease
            "r.0.fr" -> certificate.testedPositiveOnFormatted
            "r.0.df" -> certificate.validFromFormatted
            "r.0.du" -> certificate.validUntilFormatted
            "r.0.co" -> certificate.certificateCountry
            "r.0.is" -> certificate.certificateIssuer
            "r.0.ci" -> certificate.uniqueCertificateIdentifier
            else -> null
        }
        else -> null
    }
}

data class EvaluatedField(
    val field: String,
    @StringRes val fieldResourceId: Int,
    val certificateFieldValue: String?
) : HasStableId {
    override val stableId: Long
        get() = this.field.hashCode().toLong()
}
