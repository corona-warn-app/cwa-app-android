package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultCardHelper
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
        ruleDescription.text = ValidationResultCardHelper.getRuleDescription(item.evaluatedDccRule.rule)
        countryInformation.text = ValidationResultCardHelper.getCountryDescription(
            context,
            item.evaluatedDccRule.rule,
            item.certificate
        )
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
        val certificate: CwaCovidCertificate,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = evaluatedDccRule.rule.identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
