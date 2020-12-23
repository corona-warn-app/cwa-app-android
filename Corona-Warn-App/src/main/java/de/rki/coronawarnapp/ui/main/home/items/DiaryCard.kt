package de.rki.coronawarnapp.ui.main.home.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryHomescreenCardIncludeBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DiaryCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, ContactDiaryHomescreenCardIncludeBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        ContactDiaryHomescreenCardIncludeBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: ContactDiaryHomescreenCardIncludeBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        itemView.setOnClickListener {
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            curItem.onClickAction(item)
        }
        contactDiaryCardHomescreenButton.setOnClickListener { itemView.performClick() }
    }

    data class Item(val onClickAction: (Item) -> Unit) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
