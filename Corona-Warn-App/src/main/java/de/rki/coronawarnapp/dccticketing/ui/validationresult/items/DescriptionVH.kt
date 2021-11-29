package de.rki.coronawarnapp.dccticketing.ui.validationresult.items

import android.view.ViewGroup
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingValidationResultDescriptionItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class DescriptionVH(
    parent: ViewGroup
) : BaseValidationResultVH<DescriptionVH.Item, DccTicketingValidationResultDescriptionItemBinding>(
    R.layout.dcc_ticketing_validation_result_description_item,
    parent
) {

    override val viewBinding = lazy {
        DccTicketingValidationResultDescriptionItemBinding.bind(itemView)
    }

    override val onBindData: DccTicketingValidationResultDescriptionItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        with(curItem) {
            headerText.setText(header)
            bodyText.text = body.get(context)
        }
    }

    data class Item(
        @StringRes val header: Int,
        val body: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
