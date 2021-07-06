package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.EvaluatedDccRule
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleFailedItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

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
    ) -> Unit = { item, payloads ->
        // TODO
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
