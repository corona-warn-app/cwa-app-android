package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asExternalRule
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleFailedItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import java.util.Locale

class BusinessRuleFailedVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleFailedVH.Item, CovidCertificateValidationResultRuleFailedItemBinding>(
    R.layout.covid_certificate_validation_result_rule_container,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleFailedItemBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.container),
            true
        )
    }

    override val onBindData: CovidCertificateValidationResultRuleFailedItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        item.evaluatedDccRule.rule.description
        ruleDescription.text = getRuleDescription(item.evaluatedDccRule.rule)
        countryInformation.text = getCountryDescription(item.evaluatedDccRule.rule, item.certificate)
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
        val certificate: CwaCovidCertificate,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = evaluatedDccRule.rule.identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }

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
    fun getCountryDescription(rule: DccValidationRule, certificate: CwaCovidCertificate): String {

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
