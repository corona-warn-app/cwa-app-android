package de.rki.coronawarnapp.dccticketing.ui.validationresult.items

import android.view.ViewGroup
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class RuleHeaderVH(
    parent: ViewGroup
) : BaseValidationResultVH<RuleHeaderVH.Item, CovidCertificateValidationResultRuleHeaderItemBinding>(
    R.layout.dcc_ticketing_validation_result_header_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleHeaderItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultRuleHeaderItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        with(curItem) {
            titleText.setText(title)
            subtitleText.text = subtitle.get(context)
        }
    }

    data class Item(
        @StringRes val title: Int,
        val subtitle: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
