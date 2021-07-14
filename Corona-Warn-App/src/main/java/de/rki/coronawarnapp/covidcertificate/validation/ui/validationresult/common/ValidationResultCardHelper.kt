package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import java.util.Locale

object ValidationResultCardHelper {

    // Apply rules from tech spec to decide which rule description to display
    fun getRuleDescription(rule: DccValidationRule): String {
        val descArray = rule.description

        val currentLocaleCode = Locale.getDefault().language

        for (item in descArray) {
            if (item.languageCode == currentLocaleCode) {
                return item.description
            }
        }

        for (item in descArray) {
            if (item.languageCode == "en") {
                return item.description
            }
        }

        if (descArray.isNotEmpty()) {
            return descArray.first().description
        }

        return rule.identifier
    }

    // Apply rules from tech spec to decide which rule description to display
    fun getCountryDescription(context: Context, rule: DccValidationRule, certificate: CwaCovidCertificate): String {

        return when (rule.typeDcc) {
            DccValidationRule.Type.ACCEPTANCE -> {
                context.getString(
                    R.string.validation_rules_failed_vh_travel_country,
                    DccCountry(rule.country).displayName()
                )
            }

            DccValidationRule.Type.INVALIDATION -> {
                context.getString(
                    R.string.validation_rules_open_vh_subtitle,
                    DccCountry(certificate.certificateCountry).displayName()
                )
            }
        }
    }
}
