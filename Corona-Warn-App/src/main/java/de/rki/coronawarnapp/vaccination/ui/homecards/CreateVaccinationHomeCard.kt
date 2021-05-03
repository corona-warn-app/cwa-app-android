package de.rki.coronawarnapp.vaccination.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeVaccinationRegistrationCardBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CreateVaccinationHomeCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<CreateVaccinationHomeCard.Item, HomeVaccinationRegistrationCardBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeVaccinationRegistrationCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeVaccinationRegistrationCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        itemView.setOnClickListener {
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            curItem.onClickAction(item)
        }
    }

    data class Item(val onClickAction: (Item) -> Unit) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
