package de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultInputItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class ValidationInputVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationInputVH.Item, CovidCertificateValidationResultInputItemBinding>(
    R.layout.dcc_ticketing_validation_result_input_item,
    parent
) {

    // TODO: binding is wrong in all items
    override val viewBinding = lazy {
        CovidCertificateValidationResultInputItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultInputItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        dateDetailsTv.text = curItem.dateDetails.get(context)
    }

    data class Item(
        val dateDetails: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
