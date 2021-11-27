package de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleItemBinding
import de.rki.coronawarnapp.databinding.DccTicketingValidationResultRuleItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BusinessRuleVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleVH.Item, DccTicketingValidationResultRuleItemBinding>(
    R.layout.dcc_ticketing_validation_result_rule_item,
    parent
) {

    override val viewBinding = lazy {
        DccTicketingValidationResultRuleItemBinding.bind(itemView)
    }

    override val onBindData: DccTicketingValidationResultRuleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        with(curItem) {
            ruleIcon.setImageResource(ruleIconRes)
            ruleDescription.text = ruleDescriptionText
            ruleId.text = identifier
        }
    }

    data class Item(
        @DrawableRes val ruleIconRes: Int,
        val ruleDescriptionText: String,
        val identifier: String
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
