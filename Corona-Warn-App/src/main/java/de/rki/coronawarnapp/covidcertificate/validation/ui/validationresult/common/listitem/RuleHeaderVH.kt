package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RuleHeaderVH(
    parent: ViewGroup
) : BaseValidationResultVH<RuleHeaderVH.Item, CovidCertificateValidationResultRuleHeaderItemBinding>(
    R.layout.covid_certificate_validation_result_rule_header_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleHeaderItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultRuleHeaderItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        // TODO e.g.
        // Prüfung abgeschlossen
        // Folgende Regeln sind nicht erfüllt:
    }

    data class Item(
        val type: DccValidation.State,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
