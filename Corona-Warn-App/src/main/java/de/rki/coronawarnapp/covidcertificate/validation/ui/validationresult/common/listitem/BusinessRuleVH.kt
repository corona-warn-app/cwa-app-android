package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.descriptionDisplayText
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.getCountryDescription
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BusinessRuleVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleVH.Item, CovidCertificateValidationResultRuleItemBinding>(
    R.layout.covid_certificate_validation_result_rule_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultRuleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val iconRes = when (item.evaluatedDccRule.result) {
            DccValidationRule.Result.OPEN -> R.drawable.ic_grey_question_mark
            DccValidationRule.Result.FAILED -> R.drawable.ic_high_risk_alert
            else -> throw IllegalArgumentException("Expected result of rule to be OPEN or FAILED but was ${item.evaluatedDccRule.result.name}")
        }
        ruleIcon.setImageResource(iconRes)

        item.evaluatedDccRule.rule.run {
            ruleDescription.text = descriptionDisplayText
            countryInformation.text = getCountryDescription(
                context,
                item.certificate
            )

            //TODO: Show affected fields

            ruleId.text = identifier
        }
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
        val certificate: CwaCovidCertificate
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = evaluatedDccRule.rule.identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
