package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

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
    ) -> Unit = { item, _ ->
        with(item) {
            titleText.isGone = hideTitle
            titleText.text = title.get(context)
            subtitleText.text = subtitle.get(context)
        }
    }

    data class Item(
        val hideTitle: Boolean,
        val title: LazyString,
        val subtitle: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
