package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleOpenItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BusinessRuleOpenVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleOpenVH.Item, CovidCertificateValidationResultRuleOpenItemBinding>(
    R.layout.covid_certificate_validation_result_rule_container,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleOpenItemBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.container),
            true
        )
    }

    override val onBindData: CovidCertificateValidationResultRuleOpenItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        ruleId.text = item.evaluatedDccRule.rule.identifier
        dateOfSampleCollection.text = item.evaluatedDccRule.rule.validFrom
        subtitle.text = context.getString(
            R.string.validation_rules_open_vh_subtitle,
            item.evaluatedDccRule.rule.country // TODO: Add valueSet to country
        )
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = evaluatedDccRule.rule.identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
