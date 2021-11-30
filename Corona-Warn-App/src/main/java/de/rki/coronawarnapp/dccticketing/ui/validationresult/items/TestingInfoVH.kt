package de.rki.coronawarnapp.dccticketing.ui.validationresult.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingValidationResultTestingInfoItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class TestingInfoVH(
    parent: ViewGroup
) : BaseValidationResultVH<TestingInfoVH.Item, DccTicketingValidationResultTestingInfoItemBinding>(
    R.layout.dcc_ticketing_validation_result_testing_info_item,
    parent
) {

    override val viewBinding = lazy {
        DccTicketingValidationResultTestingInfoItemBinding.bind(itemView)
    }

    override val onBindData: DccTicketingValidationResultTestingInfoItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        textViewInfo.text = curItem.info.get(context)
    }

    data class Item(
        val info: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
